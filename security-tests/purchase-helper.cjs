const { doc, serverTimestamp } = require('firebase/firestore');
const { createHash } = require('node:crypto');

function hintReceipt(uid, before = 0, number = 1, attemptId = '12345678-abcd-abcd-abcd-123456789012') {
  const levelId = 101, taskId = 'inspect-sender';
  const id = createHash('sha256').update(`${attemptId}:${levelId}:${taskId}:${number}`).digest('hex');
  const cost = [15, 30, 50][Math.min(number - 1, 2)];
  return { id, data: { attemptId, levelId, taskId, hintNumber: number, cost,
    spentBefore: before, spentAfter: before + cost, createdAt: serverTimestamp() } };
}
function addPurchase(batch, db, uid, before = 0, number = 1, attemptId) {
  const receipt = hintReceipt(uid, before, number, attemptId);
  batch.set(doc(db, `userProgress/${uid}`), {
    xpSpent: receipt.data.spentAfter, lastHintPurchase: receipt.id
  }, { merge: true });
  batch.set(doc(db, `userProgress/${uid}/hintPurchases/${receipt.id}`), receipt.data);
  return receipt;
}
module.exports = { hintReceipt, addPurchase };
