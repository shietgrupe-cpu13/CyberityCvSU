const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const { doc, setDoc, getDoc, runTransaction, serverTimestamp } = require('firebase/firestore');

test('Every level can be completed, retried and replayed with existing progress', async () => {
  const rules = readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8');
  const ids = JSON.parse(rules.match(/function knownLevelIds\(\) \{ return (\[[^;]+\]);/)[1]);
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088, rules
  }});
  try {
    for (const streaks of [false, true]) {
      const uid = `completion-${streaks}`;
      const db = env.authenticatedContext(uid, { email_verified: true, auth_time: Math.floor(Date.now()/1000) }).firestore();
      const ref = doc(db, `userProgress/${uid}`);
      // A second pass replays early units with a full course history.
      for (const id of [...ids, ...ids]) {
        for (let attempt = 0; attempt < 3; attempt++) {
          if (streaks) await setDoc(ref, { clockPing: serverTimestamp() }, { merge: true });
          try {
            await runTransaction(db, async tx => {
              const old = (await tx.get(ref)).data() || {};
              const levelXp = { ...old.levelXp };
              if (!(String(id) in levelXp)) levelXp[id] = id === 200 ? 0 : 115;
              tx.set(ref, { completedLevels: [...new Set([...(old.completedLevels || []), id])], levelXp,
                ...(streaks && id !== 200 ? { streakDays: 1, streakLastActivityAt: serverTimestamp(), streakLevelId: id } : {})
              }, { merge: true });
            });
          } catch (error) { throw new Error(`Level ${id}, attempt ${attempt}, streaks ${streaks}: ${error.message}`); }
        }
      }
      const saved = (await getDoc(ref)).data();
      assert.equal(saved.completedLevels.length, ids.length);
      assert.equal(Object.values(saved.levelXp).reduce((a,b) => a+b, 0), (ids.length-1)*115);
    }
  } finally { await env.cleanup(); }
});
