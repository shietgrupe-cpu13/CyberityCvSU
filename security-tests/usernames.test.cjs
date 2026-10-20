const { test } = require('node:test');
const { readFileSync } = require('node:fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, setDoc, updateDoc, getDoc, deleteDoc, writeBatch, runTransaction } = require('firebase/firestore');
const { saveProfile } = require('./profile-helper.cjs');
test('Username claims are atomic, case insensitive, private to owned queries, and cannot be stolen or released alone', async () => {
 const env = await initializeTestEnvironment({projectId:'demo-cyberity-spark',firestore:{host:'127.0.0.1',port:8088,rules:readFileSync(require('node:path').join(__dirname,'../firestore.rules'),'utf8')}});
 try {
  const auth={email_verified:true,auth_time:Math.floor(Date.now()/1000)};
  const a=env.authenticatedContext('name-a',auth).firestore(), b=env.authenticatedContext('name-b',auth).firestore();
  const ref=doc(a,'users/name-a');
  await assertSucceeds(saveProfile(ref,{displayName:'Student Name',fullName:'Student Full Name'}));
  await assertFails(saveProfile(doc(b,'users/name-b'),{displayName:'STUDENT NAME'}));
  await assertFails(setDoc(doc(b,'usernames/student name'),{uid:'name-b'}));
  await assertFails(deleteDoc(doc(b,'usernames/student name')));
  await assertFails(deleteDoc(doc(a,'usernames/student name')));
  await assertFails(updateDoc(ref,{displayName:'Changed'}));
  await assertFails(updateDoc(ref,{usernameKey:'changed'}));
  await assertFails(setDoc(doc(b,'users/name-b'),{displayName:'Bypass'}));
  await assertFails(getDoc(doc(b,ref.path)));
  await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(),'usernames/student name')));
  await assertSucceeds(updateDoc(ref,{fullName:'Updated Full Name',yearLevel:3}));
  await assertSucceeds(saveProfile(ref,{displayName:'Another Name'})).catch(e=>{throw new Error('rename: '+e.message)});
  await assertSucceeds(saveProfile(doc(b,'users/name-b'),{displayName:'Student Name'})).catch(e=>{throw new Error('reclaim: '+e.message)});
  // Migration reserves the earliest account and marks later accounts for repair.
  const later=env.authenticatedContext('later-duplicate',auth).firestore();
  const laterRef=doc(later,'users/later-duplicate');
  await env.withSecurityRulesDisabled(async admin=>{
   await setDoc(doc(admin.firestore(),laterRef.path),{displayName:'Student Name',fullName:'Later Full Name',usernameChangeRequired:true});
  });
  await assertFails(updateDoc(laterRef,{usernameChangeRequired:false}));
  await assertFails(saveProfile(laterRef,{displayName:'STUDENT NAME',usernameChangeRequired:false}));
  await assertFails(setDoc(doc(later,'leaderboard/later-duplicate'),{displayName:'Student Name',xp:50,isTester:false,updatedAt:require('firebase/firestore').serverTimestamp()}));
  await assertSucceeds(saveProfile(laterRef,{displayName:'A Fresh Username',usernameChangeRequired:false}));
  require('node:assert/strict').equal((await getDoc(laterRef)).get('fullName'),'Later Full Name');
  // Two independent clients compete for a previously unused name.
  const claim = (db,uid) => runTransaction(db,async tx=>{
   const key=doc(db,'usernames/race name');
   if((await tx.get(key)).exists()) return false;
   tx.set(key,{uid}); tx.set(doc(db,'users/'+uid),{displayName:'Race Name',usernameKey:'race name'});return true;
  });
  const c=env.authenticatedContext('race-c',auth).firestore(),d=env.authenticatedContext('race-d',auth).firestore();
  const result=await Promise.allSettled([claim(c,'race-c'),claim(d,'race-d')]);
  require('node:assert/strict').equal(result.filter(r=>r.status === 'fulfilled' && r.value === true).length,1);
  require('node:assert/strict').ok(['race-c','race-d'].includes((await getDoc(doc(c,'usernames/race name'))).get('uid')));
 } finally { await env.cleanup(); }
});
