const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, setDoc, updateDoc, getDoc, serverTimestamp, Timestamp, deleteField } = require('firebase/firestore');

test('Learning streak validates calendar days, timestamps, ownership and legacy progress', async () => {
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088,
    rules: readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8')
  }});
  try {
    const uid = 'streak-student';
    const auth = { email_verified: true, auth_time: Math.floor(Date.now()/1000) };
    const db = env.authenticatedContext(uid, auth).firestore();
    const ref = doc(db, `userProgress/${uid}`);
    const base = { completedLevels: [101], levelXp: { '101': 100 } };
    const activity = days => ({ streakDays: days, streakLastActivityAt: serverTimestamp(), streakLevelId: 101 });
    const freshDb = env.authenticatedContext('streak-first-clear', auth).firestore();
    await assertSucceeds(setDoc(doc(freshDb, 'userProgress/streak-first-clear'), { ...base, ...activity(1) }));
    await assertSucceeds(setDoc(ref, base));
    await assertFails(updateDoc(ref, activity(99)));
    await assertFails(updateDoc(ref, { ...activity(1), streakLastActivityAt: Timestamp.fromMillis(0) }));
    for (const shift of [-30, -1, 1, 30, 365]) {
      await assertFails(updateDoc(ref, { ...activity(1),
        streakLastActivityAt: Timestamp.fromMillis(Date.now() + shift * 86400000) }));
      await assertFails(updateDoc(ref, { clockPing: Timestamp.fromMillis(Date.now() + shift * 86400000) }));
    }
    await assertFails(updateDoc(ref, { ...activity(1), streakLevelId: 200 }));
    await assertFails(updateDoc(ref, { ...activity(1), streakLevelId: 102 }));
    await assertSucceeds(updateDoc(ref, activity(1)));
    await assertSucceeds(updateDoc(ref, activity(1))); // replay / duplicate acknowledgement
    // Neither an advanced date nor a forged clock anchor can turn today into tomorrow.
    await assertFails(updateDoc(ref, { ...activity(2), streakLastActivityAt: Timestamp.fromMillis(Date.now() + 86400000) }));
    await assertFails(updateDoc(ref, activity(2)));
    await assertFails(updateDoc(ref, { streakDays: deleteField() }));
    const other = env.authenticatedContext('streak-other', auth).firestore();
    await assertFails(getDoc(doc(other, ref.path)));
    await assertFails(updateDoc(doc(other, ref.path), activity(1)));
    // Seed last activity at noon on yesterday's Philippine calendar date.
    const day = 86400000, offset = 8 * 3600000;
    const today = Math.floor((Date.now() + offset) / day) * day - offset;
    async function seed(lastAt) {
      await env.withSecurityRulesDisabled(async ctx => setDoc(doc(ctx.firestore(), ref.path), {
        ...base, streakDays: 4, streakLastActivityAt: Timestamp.fromMillis(lastAt), streakLevelId: 101
      }));
    }
    await seed(today - day / 2);
    await assertFails(updateDoc(ref, activity(6)));
    await assertSucceeds(updateDoc(ref, activity(5)));
    assert.equal((await getDoc(ref)).get('streakDays'), 5);
    await seed(today - 2 * day + day / 2);
    await assertFails(updateDoc(ref, activity(5)));
    await assertSucceeds(updateDoc(ref, activity(1)));
    await assertSucceeds(updateDoc(ref, { clockPing: serverTimestamp() }));
    const rules = readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8');
    const ids = JSON.parse(rules.match(/function knownLevelIds\(\) \{ return (\[[^;]+\]);/)[1]);
    const awards = Object.fromEntries(ids.map(id => [String(id), id === 200 ? 0 : 100]));
    const oldAwards = { ...awards }; delete oldAwards['605'];
    await env.withSecurityRulesDisabled(async ctx => setDoc(doc(ctx.firestore(), ref.path), {
      completedLevels: ids.filter(id => id !== 605), levelXp: oldAwards,
      streakDays: 4, streakLastActivityAt: Timestamp.fromMillis(today - day / 2), streakLevelId: 604
    }));
    await assertSucceeds(updateDoc(ref, { completedLevels: ids, levelXp: awards,
      ...activity(5), streakLevelId: 605 }));
    // An administrator can correct or reset the counter; clients cannot self-grant.
    await env.withSecurityRulesDisabled(async ctx => updateDoc(doc(ctx.firestore(), ref.path), activity(25)));
    assert.equal((await getDoc(ref)).get('streakDays'), 25);
    await assertFails(updateDoc(ref, activity(26)));
    await assertSucceeds(updateDoc(ref, activity(25)));
    await assertFails(updateDoc(ref, activity(0)));
    await env.withSecurityRulesDisabled(async ctx => updateDoc(doc(ctx.firestore(), ref.path), { streakDays: 0 }));
    await assertSucceeds(updateDoc(ref, activity(1)));
  } finally { await env.cleanup(); }
});
