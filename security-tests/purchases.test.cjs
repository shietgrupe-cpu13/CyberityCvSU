const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, getDoc, setDoc, updateDoc, deleteDoc, writeBatch, runTransaction, collection, getDocs } = require('firebase/firestore');
const { hintReceipt, addPurchase } = require('./purchase-helper.cjs');

test('Hint receipts require matching debits, stay private, and retry without a second charge', async () => {
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088, rules: readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8')
  }});
  try {
    const auth = { email_verified: true, auth_time: Math.floor(Date.now() / 1000) };
    const db = env.authenticatedContext('purchase-alice', auth).firestore();
    const other = env.authenticatedContext('purchase-bob', auth).firestore();
    const progress = doc(db, 'userProgress/purchase-alice');
    await assertSucceeds(setDoc(progress, { completedLevels: [101], levelXp: { 101: 100 } }));
    const request = hintReceipt('purchase-alice');
    const receipt = doc(db, `userProgress/purchase-alice/hintPurchases/${request.id}`);
    await assertFails(setDoc(receipt, request.data));
    await assertFails(updateDoc(progress, { xpSpent: 15 }));
    const wrong = writeBatch(db);
    wrong.set(receipt, request.data);
    wrong.update(progress, { xpSpent: 30, lastHintPurchase: request.id });
    await assertFails(wrong.commit());
    const purchase = () => runTransaction(db, async tx => {
      const purchased = await tx.get(receipt);
      const state = await tx.get(progress);
      if (purchased.exists()) return state.get('xpSpent');
      tx.set(receipt, request.data);
      tx.update(progress, { xpSpent: 15, lastHintPurchase: request.id });
      return 15;
    });
    const concurrent = await Promise.allSettled([purchase(), purchase()]);
    assert.ok(concurrent.some(result => result.status === 'fulfilled'));
    // A contending stale transaction may be rejected; retry reads its existing receipt.
    await purchase();
    assert.equal((await getDoc(progress)).get('xpSpent'), 15);
    assert.equal((await getDocs(collection(db, 'userProgress/purchase-alice/hintPurchases'))).size, 1);
    await assertFails(getDoc(doc(other, receipt.path)));
    await assertFails(updateDoc(receipt, { cost: 0 }));
    await assertFails(deleteDoc(receipt));
    const next = writeBatch(db); addPurchase(next, db, 'purchase-alice', 15, 2);
    await assertSucceeds(next.commit());
    assert.equal((await getDoc(progress)).get('xpSpent'), 45);
    const overdraw = writeBatch(db); addPurchase(overdraw, db, 'purchase-alice', 145, 3);
    await assertFails(overdraw.commit());
    await assertSucceeds(setDoc(doc(db, 'privacyPreferences/purchase-alice'), {
      leaderboardVisible: false, deleting: true, deletionAuthTime: auth.auth_time
    }));
    await assertSucceeds(getDocs(collection(db, 'userProgress/purchase-alice/hintPurchases')));
    await assertSucceeds(deleteDoc(receipt));
  } finally { await env.cleanup(); }
});

test('Concurrent completion retries preserve one first award and repair missing legacy awards', async () => {
  const env = await initializeTestEnvironment({ projectId: 'demo-cyberity-spark', firestore: {
    host: '127.0.0.1', port: 8088, rules: readFileSync(require('node:path').join(__dirname, '../firestore.rules'), 'utf8')
  }});
  try {
    const db = env.authenticatedContext('completion-alice', { email_verified: true, auth_time: Math.floor(Date.now()/1000) }).firestore();
    const ref = doc(db, 'userProgress/completion-alice');
    const complete = xp => runTransaction(db, async tx => {
      const saved = await tx.get(ref), awards = saved.get('levelXp') || {};
      const completed = saved.get('completedLevels') || [];
      tx.set(ref, { completedLevels: [...new Set([...completed, 101])],
        levelXp: { ...awards, 101: awards['101'] ?? xp } }, { merge: true });
    });
    await assertSucceeds(setDoc(ref, { completedLevels: [101] }));
    await Promise.all([complete(100), complete(100)]);
    await complete(150);
    const result = await getDoc(ref);
    assert.deepEqual(result.get('completedLevels'), [101]);
    assert.equal(result.get('levelXp')['101'], 100);
  } finally { await env.cleanup(); }
});
