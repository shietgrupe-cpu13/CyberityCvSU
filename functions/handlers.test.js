"use strict";
// Exercise exported handlers with isolated Admin services; no live student data.
const test = require("node:test");
const assert = require("node:assert/strict");
const Module = require("node:module");
const records = new Map();
let token, user, revoked, rejectToken;
const snapshot = path => ({ exists: records.has(path), get: key => records.get(path)?.[key],
  data: () => records.get(path) });
const db = {
  doc: path => ({ path, get: async () => snapshot(path),
    set: async value => records.set(path, { ...records.get(path), ...value }) }),
  runTransaction: async action => action({
    get: async ref => snapshot(ref.path),
    set: (ref, value) => records.set(ref.path, value),
    update: (ref, value) => records.set(ref.path, { ...records.get(ref.path), ...value })
  }),
  collection: path => ({ orderBy: () => ({ limit: () => ({ get: async () => ({
    docs: [...records.keys()].filter(key => key.startsWith(path + "/")).map(key => ({
      ...snapshot(key), id: key.split("/").pop()
    }))
  }) }) }) })
};
class HttpsError extends Error { constructor(code, message) { super(message); this.code = code; } }
const originalLoad = Module._load;
Module._load = function(name, ...args) {
  if (name === "firebase-functions/v2/https") return { onCall: (_, handler) => handler, HttpsError };
  if (name === "firebase-admin/app") return { initializeApp() {} };
  if (name === "firebase-admin/auth") return { getAuth: () => ({
    verifyIdToken: async (_, checkRevoked) => {
      assert.equal(checkRevoked, true);
      if (rejectToken) throw new Error("revoked");
      return token;
    },
    getUser: async () => user,
    revokeRefreshTokens: async uid => { revoked = uid; }
  }) };
  if (name === "firebase-admin/firestore") return { getFirestore: () => db, Timestamp: { fromMillis: value => value } };
  return originalLoad.call(this, name, ...args);
};
const handlers = require("./index");
Module._load = originalLoad;
const sessionId = "12345678-abcd-abcd-abcd-123456789012";
function setup() {
  records.clear(); revoked = null; rejectToken = false;
  token = { uid: "student-a", email_verified: true, auth_time: Math.floor(Date.now() / 1000), firebase: {} };
  user = { tokensValidAfterTime: new Date(0).toISOString(), multiFactor: { enrolledFactors: [] } };
  return { auth: { uid: "student-a" }, rawRequest: { headers: { authorization: "Bearer test" } },
    data: { sessionId, device: "Test Android", androidVersion: "16" } };
}
test("anonymous, mismatched, unverified and revoked tokens cannot register devices", async () => {
  let request = setup(); request.auth = null;
  await assert.rejects(handlers.touchAccountSession(request), { code: "unauthenticated" });
  request = setup(); token.uid = "student-b";
  await assert.rejects(handlers.touchAccountSession(request), { code: "permission-denied" });
  request = setup(); token.email_verified = false;
  await assert.rejects(handlers.touchAccountSession(request), { code: "permission-denied" });
  request = setup(); rejectToken = true;
  await assert.rejects(handlers.touchAccountSession(request), { code: "unauthenticated" });
  assert.equal(records.size, 0);
});
test("device listing is scoped to the verified account and local logout hides the record", async () => {
  const request = setup();
  records.set("accountSecurity/student-b/sessions/other", { device: "Other student", lastActive: Date.now(), authTime: token.auth_time });
  await handlers.touchAccountSession(request);
  let result = await handlers.listAccountSessions(request);
  assert.equal(result.sessions.length, 1);
  assert.equal(result.sessions[0].device, "Test Android");
  await handlers.endAccountSession(request);
  result = await handlers.listAccountSessions(request);
  assert.equal(result.sessions.length, 0);
});
test("global revocation requires recent authentication and enrolled MFA", async () => {
  let request = setup(); token.auth_time -= 301;
  await assert.rejects(handlers.revokeAllAccountSessions(request), { code: "failed-precondition" });
  request = setup(); user.multiFactor.enrolledFactors = [{ factorId: "totp" }];
  await assert.rejects(handlers.revokeAllAccountSessions(request), { code: "failed-precondition" });
  token.firebase.sign_in_second_factor = "totp";
  await handlers.revokeAllAccountSessions(request);
  assert.equal(revoked, "student-a");
  assert.equal(records.has("accountSecurity/student-a"), true);
  await assert.rejects(handlers.touchAccountSession(request), { code: "unauthenticated" });
});
