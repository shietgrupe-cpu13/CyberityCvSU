"use strict";
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");
const { RETENTION_MS, sessionInput, isFresh, isActive } = require("./policy");
initializeApp();
const db = getFirestore();
const options = { region: "asia-southeast1", maxInstances: 5 };

async function authenticate(request) {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in again.");
  const header = request.rawRequest.headers.authorization || "";
  if (!header.startsWith("Bearer ")) throw new HttpsError("unauthenticated", "Missing token.");
  let token;
  try { token = await getAuth().verifyIdToken(header.slice(7), true); }
  catch { throw new HttpsError("unauthenticated", "Session expired."); }
  if (token.uid !== request.auth.uid || !token.email_verified) {
    throw new HttpsError("permission-denied", "A verified account is required.");
  }
  // Server-owned cutoff also rejects same-second tokens during global logout.
  const metadata = await db.doc(`accountSecurity/${token.uid}`).get();
  if (metadata.exists && token.auth_time <= metadata.get("revokedBefore")) {
    throw new HttpsError("unauthenticated", "Session expired.");
  }
  return token;
}

exports.touchAccountSession = onCall(options, async request => {
  const token = await authenticate(request);
  let input;
  try { input = sessionInput(request.data); }
  catch { throw new HttpsError("invalid-argument", "Invalid device details."); }
  const ref = db.doc(`accountSecurity/${token.uid}/sessions/${input.sessionId}`);
  const now = Date.now();
  await db.runTransaction(async transaction => {
    const existing = await transaction.get(ref);
    const metadata = await transaction.get(db.doc(`accountSecurity/${token.uid}`));
    if (metadata.exists && token.auth_time <= metadata.get("revokedBefore")) {
      throw new HttpsError("unauthenticated", "Session expired.");
    }
    transaction.set(ref, {
      device: input.device, androidVersion: input.androidVersion,
      authTime: token.auth_time, lastActive: now, signedOut: false,
      createdAt: existing.get("createdAt") || now,
      expiresAt: Timestamp.fromMillis(now + RETENTION_MS)
    });
  });
  return { ok: true };
});

exports.listAccountSessions = onCall(options, async request => {
  const token = await authenticate(request);
  const user = await getAuth().getUser(token.uid);
  const validAfter = Date.parse(user.tokensValidAfterTime) / 1000;
  const metadata = await db.doc(`accountSecurity/${token.uid}`).get();
  const cutoff = metadata.get("revokedBefore") || 0;
  const snapshot = await db.collection(`accountSecurity/${token.uid}/sessions`)
    .orderBy("lastActive", "desc").limit(50).get();
  return { sessions: snapshot.docs.filter(doc => doc.get("authTime") > cutoff && isActive(doc.data(), validAfter, Date.now()))
    .map(doc => ({ id: doc.id, device: doc.get("device"), lastActive: doc.get("lastActive") })) };
});

exports.endAccountSession = onCall(options, async request => {
  const token = await authenticate(request);
  const id = request.data?.sessionId;
  if (typeof id !== "string" || !/^[a-f0-9-]{36}$/.test(id)) {
    throw new HttpsError("invalid-argument", "Invalid session ID.");
  }
  const ref = db.doc(`accountSecurity/${token.uid}/sessions/${id}`);
  await db.runTransaction(async transaction => {
    const session = await transaction.get(ref);
    if (session.exists && session.get("authTime") === token.auth_time) {
      transaction.update(ref, { signedOut: true });
    }
  });
  return { ok: true };
});

exports.revokeAllAccountSessions = onCall(options, async request => {
  const token = await authenticate(request);
  if (!isFresh(token.auth_time, Date.now() / 1000)) {
    throw new HttpsError("failed-precondition", "Verify your password again.");
  }
  const user = await getAuth().getUser(token.uid);
  if (user.multiFactor?.enrolledFactors?.length && !token.firebase?.sign_in_second_factor) {
    throw new HttpsError("failed-precondition", "Verify your authenticator code.");
  }
  // Write cutoff first so database rules can reject existing ID tokens immediately.
  await db.doc(`accountSecurity/${token.uid}`).set({ revokedBefore: Math.floor(Date.now() / 1000) }, { merge: true });
  await getAuth().revokeRefreshTokens(token.uid);
  return { ok: true };
});
