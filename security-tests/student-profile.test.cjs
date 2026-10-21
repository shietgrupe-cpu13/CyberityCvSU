const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, setDoc, getDoc, updateDoc, serverTimestamp } = require('firebase/firestore');

test('Year level supports legacy profiles, validates create/update, and remains private', async () => {
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088,
    rules: readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8')
  }});
  try {
    const auth = { email_verified: true, auth_time: Math.floor(Date.now()/1000) };
    const db = env.authenticatedContext('year-student', auth).firestore();
    const other = env.authenticatedContext('year-other', auth).firestore();
    const ref = doc(db, 'users/year-student');
    const profile = { studentId: '202310502', displayName: 'Student User', isTester: false };
    await assertSucceeds(setDoc(ref, profile));
    await assertSucceeds(updateDoc(ref, { yearLevel: 3, updatedAt: serverTimestamp() }));
    assert.equal((await getDoc(ref)).get('studentId'), profile.studentId);
    assert.equal((await getDoc(ref)).get('yearLevel'), 3);
    await assertSucceeds(updateDoc(ref, { yearLevel: 4 }));
    for (const yearLevel of [0, 6, -1, 1.5, '3', null, true]) {
      await assertFails(updateDoc(ref, { yearLevel }));
      const fresh = env.authenticatedContext(`invalid-year-${String(yearLevel)}`, auth).firestore();
      await assertFails(setDoc(doc(fresh, `users/invalid-year-${String(yearLevel)}`), { ...profile, yearLevel }));
    }
    await assertFails(getDoc(doc(other, ref.path)));
    await assertFails(updateDoc(doc(other, ref.path), { yearLevel: 1 }));
    await assertSucceeds(updateDoc(ref, { yearLevel: 5 }));
    assert.equal((await getDoc(ref)).get('displayName'), profile.displayName);
  } finally { await env.cleanup(); }
});
