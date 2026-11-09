package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.designsystem.FlowButton
import com.example.ui.designsystem.FlowButtonVariant
import com.example.ui.designsystem.FlowCard
import com.example.ui.designsystem.FlowTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.firestore_database_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            kotlinx.coroutines.delay(500L)
            val activity = context as? Activity ?: return@launch
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Configuration error: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onGuestLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AuthScreenContent(
            isLoading = isLoading,
            onGoogleSignIn = {
                isLoading = true
                onGoogleSignInClicked(
                    context = context,
                    credentialManager = credentialManager,
                    onAuthSuccess = {
                        isLoading = false
                        onAuthSuccess()
                    },
                    onAuthError = { errorMsg ->
                        isLoading = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(errorMsg)
                        }
                    },
                    scope = coroutineScope,
                    onAuthCancelled = {
                        isLoading = false
                    }
                )
            },
            onGuestLogin = onGuestLogin
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun AuthScreenContent(
    isLoading: Boolean = false,
    onGoogleSignIn: () -> Unit = {},
    onGuestLogin: () -> Unit = {}
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Minimalist Brand Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(shapes.md))
                .background(colors.accentContainer)
                .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(shapes.md)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "FlowPulse Logo",
                tint = colors.onAccentContainer,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "FlowPulse",
            style = FlowTheme.typography.display.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Focus Deeply. Build Streaks. Ship Daily.",
            style = FlowTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        // Features Card
        FlowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(shapes.xl),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                FeatureRow(
                    icon = Icons.Default.Timer,
                    iconTint = colors.accent,
                    title = "Smart Focus Timer",
                    subtitle = "Tactile Pomodoro intervals with custom presets"
                )
                Spacer(modifier = Modifier.height(14.dp))
                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    iconTint = colors.priorityMedium,
                    title = "Priority Task Queue",
                    subtitle = "Organize deep work and calculate estimated pomodoros"
                )
                Spacer(modifier = Modifier.height(14.dp))
                FeatureRow(
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = colors.priorityHigh,
                    title = "Habit Streak Engine",
                    subtitle = "Build lasting discipline with daily visual progress"
                )
                Spacer(modifier = Modifier.height(14.dp))
                FeatureRow(
                    icon = Icons.Default.CloudDone,
                    iconTint = colors.success,
                    title = "Cloud & Offline Modes",
                    subtitle = "Seamless guest mode or real-time Firestore sync"
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Google Sign-In Button
        Surface(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            shape = RoundedCornerShape(shapes.md),
            color = colors.accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("google_sign_in_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.onAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = colors.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            style = FlowTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Continue as Guest Button
        FlowButton(
            text = "Continue as Guest",
            onClick = onGuestLogin,
            variant = FlowButtonVariant.SECONDARY,
            icon = Icons.Default.PersonOutline,
            enabled = !isLoading,
            height = 52.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guest_mode_button")
        )
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(shapes.sm))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = FlowTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                style = FlowTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}
