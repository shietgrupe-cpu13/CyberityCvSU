const { test } = require('node:test');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, setDoc, getDoc, deleteDoc, serverTimestamp, Timestamp, updateDoc } = require('firebase/firestore');

test('Spark cutoff protects sessions and production collections', async () => {
  const rules = readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8');
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088,
    rules
  }});
  try {
    const now = Math.floor(Date.now() / 1000);
    const auth = { email_verified: true, auth_time: now - 10 };
    const db = env.authenticatedContext('alice', auth).firestore();
    const other = env.authenticatedContext('bob', auth).firestore();
    const anon = env.unauthenticatedContext().firestore();
    const id = '12345678-abcd-abcd-abcd-123456789012';
    const metadata = 'accountSecuritySpark/alice';
    const session = `${metadata}/sessions/${id}`;
    const row = { device: 'Test Android', androidVersion: '16', authTime: auth.auth_time,
      lastActive: serverTimestamp(), signedOut: false };
    await assertSucceeds(setDoc(doc(db, session), row));
    await assertSucceeds(setDoc(doc(db, 'users/alice'), { name: 'Alice' }));
    await assertSucceeds(setDoc(doc(db, 'userProgress/alice'), { xp: 10 }));
    await assertSucceeds(setDoc(doc(db, 'studentIds/2026-001'), { uid: 'alice' }));
    const entry = { displayName: 'Alice', xp: 10, isTester: false, updatedAt: serverTimestamp() };
    await assertSucceeds(setDoc(doc(db, 'leaderboard/alice'), entry));
    await assertSucceeds(getDoc(doc(other, 'leaderboard/alice')));
    await assertSucceeds(getDoc(doc(other, 'studentIds/2026-001')));
    await assertFails(getDoc(doc(other, 'users/alice')));
    await assertFails(setDoc(doc(other, 'userProgress/alice'), { xp: 999 }));
    await assertFails(setDoc(doc(other, 'leaderboard/alice'), entry));
    await assertFails(deleteDoc(doc(other, 'studentIds/2026-001')));
    await assertFails(setDoc(doc(db, 'leaderboard/alice'), { ...entry, xp: -1 }));
    await assertFails(setDoc(doc(db, 'leaderboard/alice'), { ...entry, displayName: 'A' }));
    await assertFails(setDoc(doc(db, 'leaderboard/alice'), { ...entry, secret: 'extra' }));
    await assertFails(getDoc(doc(anon, 'leaderboard/alice')));
    await assertFails(getDoc(doc(other, session)));
    await assertFails(getDoc(doc(anon, metadata)));
    await assertFails(getDoc(doc(env.authenticatedContext('alice', { ...auth, email_verified: false }).firestore(), metadata)));
    await assertFails(setDoc(doc(db, session), { ...row, authTime: now + 1000 }));
    await assertFails(setDoc(doc(db, session), { ...row, lastActive: Timestamp.fromMillis(0) }));
    await assertFails(setDoc(doc(db, metadata), { revokedAt: Timestamp.fromMillis(0) }));
    const stale = env.authenticatedContext('alice', { ...auth, auth_time: now - 600 }).firestore();
    await assertFails(setDoc(doc(stale, metadata), { revokedAt: serverTimestamp() }));
    await assertSucceeds(updateDoc(doc(db, session), { signedOut: true }));
    await assertSucceeds(setDoc(doc(db, metadata), { revokedAt: serverTimestamp() }));
    // Old tokens can inspect their cutoff, but cannot restore access or tamper with it.
    await assertSucceeds(getDoc(doc(db, metadata)));
    await assertFails(getDoc(doc(db, session)));
    for (const path of ['users/alice', 'userProgress/alice', 'studentIds/2026-001', 'leaderboard/alice']) {
      await assertFails(getDoc(doc(db, path)));
    }
    await assertFails(setDoc(doc(db, 'users/alice'), { name: 'Bypass' }));
    await assertFails(setDoc(doc(db, 'userProgress/alice'), { xp: 999 }));
    await assertFails(setDoc(doc(db, 'leaderboard/alice'), entry));
    await assertFails(setDoc(doc(db, 'studentIds/2026-002'), { uid: 'alice' }));
    await assertFails(deleteDoc(doc(db, 'studentIds/2026-001')));
    await assertFails(setDoc(doc(db, session), row));
    await assertFails(setDoc(doc(db, metadata), { revokedAt: serverTimestamp() }));
    await assertFails(deleteDoc(doc(db, metadata)));
    await assertFails(setDoc(doc(db, metadata), {}));
    const cutoff = (await getDoc(doc(db, metadata))).get('revokedAt');
    const sameSecond = env.authenticatedContext('alice', { ...auth, auth_time: cutoff.seconds }).firestore();
    await assertFails(getDoc(doc(sameSecond, 'users/alice')));
    const freshTime = cutoff.seconds + 1;
    const fresh = env.authenticatedContext('alice', { ...auth, auth_time: freshTime }).firestore();
    for (const path of ['users/alice', 'userProgress/alice', 'studentIds/2026-001', 'leaderboard/alice']) {
      await assertSucceeds(getDoc(doc(fresh, path)));
    }
    await assertSucceeds(setDoc(doc(fresh, session), { ...row, authTime: freshTime }));
  } finally { await env.cleanup(); }
});
