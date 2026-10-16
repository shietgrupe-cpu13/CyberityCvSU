const { test } = require('node:test');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, setDoc, updateDoc, deleteDoc, writeBatch, serverTimestamp } = require('firebase/firestore');
const { addPurchase } = require('./purchase-helper.cjs');

test('Scores reject inflation, refunds, repeat awards and profile-role bypasses', async () => {
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088, rules: readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8')
  }});
  try {
    const auth = { email_verified: true, auth_time: Math.floor(Date.now()/1000) - 10 };
    const db = env.authenticatedContext('score-alice', auth).firestore();
    const progress = doc(db, 'userProgress/score-alice'), board = doc(db, 'leaderboard/score-alice');
    const profile = doc(db, 'users/score-alice');
    await assertSucceeds(setDoc(profile, { displayName: 'Score Alice', studentId: '202610001', isTester: false }));
    const entry = xp => ({ displayName: 'Score Alice', xp, isTester: false, updatedAt: serverTimestamp() });
    await assertSucceeds(setDoc(board, entry(50))); // starting grant, even before progress exists
    await assertFails(setDoc(board, entry(999999)));
    await assertFails(setDoc(progress, { levelXp: { 101: 150 } })); // no matching completion
    await assertFails(setDoc(progress, { completedLevels: [101], levelXp: { 101: 151 } }));
    await assertFails(setDoc(progress, { completedLevels: [999], levelXp: { 999: 100 } }));
    await assertFails(setDoc(progress, { completedLevels: [101], levelXp: { 101: -1 } }));
    await assertFails(setDoc(progress, { completedLevels: [101], levelXp: { 101: 1.5 } }));
    await assertFails(setDoc(progress, { completedLevels: [200], levelXp: { 200: 100 } }));
    await assertFails(setDoc(progress, { completedLevels: [101,101], levelXp: { 101: 100 } }));
    const award = writeBatch(db);
    award.set(progress, { completedLevels: [101], levelXp: { 101: 100 } });
    award.set(board, entry(150));
    await assertSucceeds(award.commit());
    await assertFails(updateDoc(progress, { 'levelXp.101': 150 })); // immutable first clear
    await assertFails(setDoc(progress, { completedLevels: [] }));
    await assertFails(deleteDoc(progress));
    await assertFails(deleteDoc(profile));
    await assertFails(updateDoc(progress, { xpSpent: -1 }));
    await assertFails(updateDoc(progress, { xpSpent: 200 }));
    const spend = writeBatch(db);
    addPurchase(spend, db, 'score-alice'); spend.set(board, entry(135));
    await assertSucceeds(spend.commit());
    await assertFails(updateDoc(progress, { xpSpent: 0 }));
    await assertFails(setDoc(board, entry(150))); // cannot ignore spent hints
    await assertFails(setDoc(board, { ...entry(135), displayName: 'False Name' }));
    await assertFails(setDoc(board, { ...entry(135), isTester: true }));
    await assertSucceeds(updateDoc(progress, { clockPing: serverTimestamp() }));
    await assertFails(updateDoc(progress, { clockPing: 0 }));
    await assertSucceeds(updateDoc(progress, { hearts: 4, heartsUpdatedAt: serverTimestamp() }));
    await assertFails(updateDoc(progress, { hearts: 999, heartsUpdatedAt: serverTimestamp() }));
    await assertFails(updateDoc(progress, { levelXp: {} }));

    const tester = env.authenticatedContext('score-tester', auth).firestore();
    const testerProfile = doc(tester, 'users/score-tester');
    await assertFails(setDoc(testerProfile, { displayName: 'Dev Tester', studentId: 'DEV-123', isTester: true }));
    await env.withSecurityRulesDisabled(async context => {
      await setDoc(doc(context.firestore(), 'users/score-tester'), { displayName: 'Dev Tester', studentId: 'DEV-123', isTester: true });
    });
    await assertFails(updateDoc(testerProfile, { studentId: '202610002', isTester: false }));
    await assertFails(deleteDoc(testerProfile));
    await assertFails(setDoc(doc(tester, 'leaderboard/score-tester'), { displayName: 'Dev Tester', xp: 50, isTester: false, updatedAt: serverTimestamp() }));

    // Exercise the entire curriculum to detect rule expression-limit regressions.
    const ids = [101,102,103,104,105,106,200,201,202,203,204,205,250,301,302,303,304,305,350,401,402,403,404,405,450,501,502,503,504,505,550,601,602,603,604,605];
    const full = env.authenticatedContext('score-full', auth).firestore();
    await assertSucceeds(setDoc(doc(full, 'users/score-full'), { displayName: 'Full Curriculum' }));
    const allAwards = Object.fromEntries(ids.map(id => [id, id===200 ? 0 : 150]));
    await assertFails(setDoc(doc(full, 'userProgress/score-full'), { completedLevels: ids, levelXp: allAwards }));
    await env.withSecurityRulesDisabled(async context => {
      await setDoc(doc(context.firestore(), 'userProgress/score-full'), { completedLevels: ids, levelXp: allAwards });
    });
    await assertSucceeds(updateDoc(doc(full, 'userProgress/score-full'), { clockPing: serverTimestamp() }));
    const fullSpend = writeBatch(full);
    addPurchase(fullSpend, full, 'score-full');
    await assertSucceeds(fullSpend.commit());
    await assertSucceeds(setDoc(doc(full, 'leaderboard/score-full'), { displayName: 'Full Curriculum', xp: 35+(ids.length-1)*150, isTester: false, updatedAt: serverTimestamp() }));
  } finally { await env.cleanup(); }
});
