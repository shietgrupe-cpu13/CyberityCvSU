const { test } = require('node:test');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, setDoc, getDoc, deleteDoc, writeBatch, serverTimestamp, collection, query, where, getDocs } = require('firebase/firestore');

test('Privacy visibility and deletion remain enforced against stale clients', async () => {
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088,
    rules: readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8')
  }});
  try {
    const now = Math.floor(Date.now() / 1000);
    const uid = 'privacy-alice';
    const auth = { email_verified: true, auth_time: now - 10 };
    const db = env.authenticatedContext(uid, auth).firestore();
    const other = env.authenticatedContext('privacy-bob', auth).firestore();
    const old = env.authenticatedContext(uid, { ...auth, auth_time: now - 60 }).firestore();
    const stale = env.authenticatedContext(uid, { ...auth, auth_time: now - 600 }).firestore();
    const pref = `privacyPreferences/${uid}`;
    const board = `leaderboard/${uid}`;
    const id = '12345678-abcd-abcd-abcd-123456789012';
    const session = `accountSecuritySpark/${uid}/sessions/${id}`;
    const row = { device: 'Android', androidVersion: '16', authTime: auth.auth_time, lastActive: serverTimestamp(), signedOut: false };
    const entry = { displayName: 'Privacy Alice', xp: 10, isTester: false, updatedAt: serverTimestamp() };
    await assertSucceeds(setDoc(doc(db, `users/${uid}`), { displayName: 'Privacy Alice' }));
    await assertSucceeds(setDoc(doc(db, `userProgress/${uid}`), { xp: 10 }));
    await assertSucceeds(setDoc(doc(db, 'studentIds/PRIVACY-001'), { uid }));
    await assertSucceeds(setDoc(doc(db, board), entry));
    await assertSucceeds(setDoc(doc(db, session), row));
    await assertFails(getDoc(doc(other, pref)));
    await assertFails(setDoc(doc(other, pref), { leaderboardVisible: false, deleting: false }));
    await assertFails(setDoc(doc(db, pref), { leaderboardVisible: false, deleting: false, arbitrary: true }));
    const hide = writeBatch(db);
    hide.set(doc(db, pref), { leaderboardVisible: false, deleting: false });
    hide.delete(doc(db, board));
    await assertSucceeds(hide.commit());
    await assertFails(setDoc(doc(old, board), entry));
    const show = writeBatch(db);
    show.set(doc(db, pref), { leaderboardVisible: true, deleting: false });
    show.set(doc(db, board), entry);
    await assertSucceeds(show.commit());

    const marker = { leaderboardVisible: false, deleting: true, deletionAuthTime: auth.auth_time };
    await assertFails(setDoc(doc(stale, pref), { ...marker, deletionAuthTime: now - 600 }));
    await assertSucceeds(setDoc(doc(db, pref), marker));
    await assertSucceeds(getDoc(doc(db, pref)));
    await assertFails(getDoc(doc(old, `users/${uid}`)));
    await assertFails(deleteDoc(doc(old, `users/${uid}`)));
    await assertFails(setDoc(doc(old, pref), { ...marker, deletionAuthTime: now - 60 }));
    await assertFails(setDoc(doc(db, pref), { leaderboardVisible: true, deleting: false }));
    await assertFails(deleteDoc(doc(db, pref)));
    await assertFails(setDoc(doc(db, session), row));
    await assertFails(setDoc(doc(db, `users/${uid}`), { displayName: 'Recreate' }));
    await assertFails(setDoc(doc(db, board), entry));
    await assertFails(setDoc(doc(db, 'studentIds/PRIVACY-002'), { uid }));
    await assertSucceeds(getDocs(query(collection(db, 'studentIds'), where('uid', '==', uid))));
    await assertSucceeds(getDocs(collection(db, `accountSecuritySpark/${uid}/sessions`)));
    for (const path of [session, 'studentIds/PRIVACY-001', `users/${uid}`, `userProgress/${uid}`, board]) {
      await assertSucceeds(deleteDoc(doc(db, path)));
    }
    await assertSucceeds(setDoc(doc(db, `accountSecuritySpark/${uid}`), { revokedAt: serverTimestamp() }));
    await assertFails(deleteDoc(doc(db, `accountSecuritySpark/${uid}`)));
    const retryAuthTime = Math.floor(Date.now() / 1000) + 1;
    const retry = env.authenticatedContext(uid, { ...auth, auth_time: retryAuthTime }).firestore();
    await assertSucceeds(setDoc(doc(retry, pref), { ...marker, deletionAuthTime: retryAuthTime }));
    await assertFails(setDoc(doc(retry, `users/${uid}`), { displayName: 'Restore' }));
    await assertFails(setDoc(doc(old, board), entry));
    await assertSucceeds(getDoc(doc(retry, pref)));
  } finally { await env.cleanup(); }
});
