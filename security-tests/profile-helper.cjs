const { doc, writeBatch, getDoc } = require('firebase/firestore');
exports.saveProfile = async (ref, data) => {
  const previous = (await getDoc(ref)).data() || {};
  const profile = { ...previous, ...data };
  const key = profile.displayName.trim().toLowerCase();
  const batch = writeBatch(ref.firestore);
  batch.set(doc(ref.firestore, 'usernames', key), { uid: ref.id });
  batch.set(ref, { ...profile, usernameKey: key });
  if (previous.usernameKey && previous.usernameKey !== key)
    batch.delete(doc(ref.firestore, 'usernames', previous.usernameKey));
  return batch.commit();
};
