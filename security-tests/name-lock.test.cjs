const {test} = require('node:test');
const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const {initializeTestEnvironment, assertSucceeds, assertFails} = require('@firebase/rules-unit-testing');
const {doc, setDoc, updateDoc, getDoc, serverTimestamp, Timestamp, deleteField, writeBatch} = require('firebase/firestore');
const {saveProfile} = require('./profile-helper.cjs');

test('Full name locks at first-level start and cannot be reset or bypassed', async () => {
  const env = await initializeTestEnvironment({projectId:'demo-cyberity-spark', firestore:{host:'127.0.0.1',port:8088,
    rules:readFileSync(require('node:path').join(__dirname,'../firestore.rules'),'utf8')}});
  try {
    const token = {email_verified:true,auth_time:Math.floor(Date.now()/1000)};
    const db = env.authenticatedContext('name-lock',token).firestore();
    const other = env.authenticatedContext('name-other',token).firestore();
    const anon = env.unauthenticatedContext().firestore();
    const ref = doc(db,'users/name-lock');
    const profile = {displayName:'Student Lock',studentId:'DEV-123',isTester:false,
      firstName:'Ana',middleName:'',lastName:'Cruz',fullName:'Ana Cruz',yearLevel:2};
    await assertSucceeds(saveProfile(ref, profile));
    await assertSucceeds(updateDoc(ref,{middleName:'Maria',fullName:'Ana Maria Cruz'}));
    // A start cannot also replace the identity being confirmed.
    await assertFails(updateDoc(ref,{fullNameLockedAt:serverTimestamp(),firstName:'Other',fullName:'Other Maria Cruz'}));
    for (const value of [false,true,'today',123,null,Timestamp.fromMillis(1000)])
      await assertFails(updateDoc(ref,{fullNameLockedAt:value}));
    await assertFails(updateDoc(doc(other,ref.path),{fullNameLockedAt:serverTimestamp()}));
    await assertFails(getDoc(doc(other,ref.path)));
    await assertFails(getDoc(doc(anon,ref.path)));
    await assertSucceeds(updateDoc(ref,{fullNameLockedAt:serverTimestamp()}));
    const lockedAt = (await getDoc(ref)).get('fullNameLockedAt');
    for (const data of [
      {firstName:'Other',fullName:'Other Maria Cruz'}, {middleName:'',fullName:'Ana Cruz'},
      {lastName:'Santos',fullName:'Ana Maria Santos'}, {fullName:'Another Name'},
      {fullNameLockedAt:deleteField()}, {fullNameLockedAt:null}, {fullNameLockedAt:serverTimestamp()},
      {firstName:deleteField(),middleName:deleteField(),lastName:deleteField(),fullName:'Different'},
      {firstName:'x'.repeat(100000),fullName:'Different'}
    ]) await assertFails(updateDoc(ref,data));
    await assertFails(setDoc(ref,profile)); // Whole-document replacement cannot drop the marker.
    await assertSucceeds(updateDoc(ref,{yearLevel:4}));
    await assertSucceeds(updateDoc(ref,{hasCompletedOnboarding:true}));
    await assertSucceeds(saveProfile(ref,{displayName:'New Username'}));
    assert.equal((await getDoc(ref)).get('fullName'),'Ana Maria Cruz');
    assert.ok((await getDoc(ref)).get('fullNameLockedAt').isEqual(lockedAt));

    // Legacy clients with completed level 101 are already locked, even without a marker.
    const legacy = env.authenticatedContext('name-legacy',token).firestore();
    const legacyRef = doc(legacy,'users/name-legacy');
    await env.withSecurityRulesDisabled(async admin => {
      await setDoc(doc(admin.firestore(),legacyRef.path),profile);
      await setDoc(doc(admin.firestore(),'userProgress/name-legacy'),{completedLevels:[101]});
    });
    await assertFails(updateDoc(legacyRef,{middleName:'Maria',fullName:'Ana Maria Cruz'}));
    await assertSucceeds(updateDoc(legacyRef,{yearLevel:3}));

    // Completion and a name change in one batch must not bypass the legacy guard.
    const batchDb = env.authenticatedContext('name-batch',token).firestore();
    const batchRef = doc(batchDb,'users/name-batch');
    await env.withSecurityRulesDisabled(async admin => { await setDoc(doc(admin.firestore(),batchRef.path),profile); });
    const batch = writeBatch(batchDb);
    batch.set(doc(batchDb,'userProgress/name-batch'),{completedLevels:[101]});
    batch.update(batchRef,{middleName:'Maria',fullName:'Ana Maria Cruz'});
    await assertFails(batch.commit());
    // Admin correction remains possible through trusted tools, never client self-service.
    await env.withSecurityRulesDisabled(async admin => {
      await updateDoc(doc(admin.firestore(),ref.path),{firstName:'Anna',fullName:'Anna Maria Cruz'});
    });
    assert.equal((await getDoc(ref)).get('fullName'),'Anna Maria Cruz');
  } finally { await env.cleanup(); }
});
