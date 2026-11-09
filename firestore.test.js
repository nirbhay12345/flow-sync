const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read tasks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get());
});

test("Unauthenticated user: cannot write task", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      userId: ALICE_UID,
      title: "Sample Task",
      category: "Work",
      priority: "high",
      estimatedPomodoros: 2,
      completedPomodoros: 0,
      isCompleted: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create valid task under their own collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      userId: ALICE_UID,
      title: "Complete Roadmap",
      category: "Work",
      priority: "high",
      estimatedPomodoros: 4,
      completedPomodoros: 0,
      isCompleted: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot write to another user's task collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("tasks").doc("task_1").set({
      userId: BOB_UID,
      title: "Hacked Task",
      category: "Work",
      priority: "high",
      estimatedPomodoros: 2,
      completedPomodoros: 0,
      isCompleted: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: rejects task with invalid priority", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_bad").set({
      userId: ALICE_UID,
      title: "Bad Task",
      category: "Work",
      priority: "urgent_critical_unsupported",
      estimatedPomodoros: 2,
      completedPomodoros: 0,
      isCompleted: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and update habit", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const habitRef = aliceDb.collection("users").doc(ALICE_UID).collection("habits").doc("habit_1");
  const now = new Date();
  await assertSucceeds(
    habitRef.set({
      userId: ALICE_UID,
      title: "Morning Run",
      category: "Health",
      targetDaysPerWeek: 5,
      currentStreak: 0,
      bestStreak: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    habitRef.update({
      currentStreak: 1,
      bestStreak: 1,
      lastCompletedDate: "2026-11-08",
      completedDates: ["2026-11-08"],
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can log focus session", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sessions").doc("sess_1").set({
      userId: ALICE_UID,
      taskTitle: "Design Database Architecture",
      category: "Work",
      durationMinutes: 25,
      type: "focus",
      completedAt: now,
      createdAt: now,
    })
  );
});
