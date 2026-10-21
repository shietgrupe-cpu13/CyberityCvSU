const {test}=require('node:test');
const assert=require('node:assert/strict');
const {readFileSync}=require('node:fs');
const {initializeTestEnvironment,assertFails,assertSucceeds}=require('@firebase/rules-unit-testing');
const {doc,getDoc,setDoc,updateDoc,deleteDoc}=require('firebase/firestore');
test('Only administrators can assign or remove isTester; profile edits preserve existing tester status',async()=>{
 const env=await initializeTestEnvironment({projectId:'demo-cyberity-spark',firestore:{host:'127.0.0.1',port:8088,rules:readFileSync(require('node:path').join(__dirname,'../firestore.rules'),'utf8')}});
 try{
  const auth={email_verified:true,auth_time:Math.floor(Date.now()/1000)};
  const db=env.authenticatedContext('dev-alice',auth).firestore();
  const bob=env.authenticatedContext('dev-bob',auth).firestore();
  const ref=doc(db,'users/dev-alice');
  await assertFails(setDoc(ref,{displayName:'Dev Alice',studentId:'DEV-001',isTester:true}));
  await assertSucceeds(setDoc(ref,{displayName:'Dev Alice',studentId:'DEV-001',isTester:false}));
  await assertFails(updateDoc(ref,{isTester:true}));
  await assertSucceeds(updateDoc(ref,{yearLevel:3}));
  await env.withSecurityRulesDisabled(async admin=>{await updateDoc(doc(admin.firestore(),ref.path),{isTester:true});});
  assert.equal((await getDoc(ref)).get('isTester'),true);
  await assertSucceeds(updateDoc(ref,{yearLevel:4,displayName:'Dev Alice Updated'}));
  await assertFails(updateDoc(ref,{isTester:false}));
  await assertFails(getDoc(doc(bob,ref.path)));
  await assertFails(updateDoc(doc(bob,ref.path),{isTester:true}));
  await assertFails(deleteDoc(ref));
  await env.withSecurityRulesDisabled(async admin=>{await updateDoc(doc(admin.firestore(),ref.path),{isTester:false});});
  assert.equal((await getDoc(ref)).get('isTester'),false);
  const legacy=env.authenticatedContext('dev-legacy',auth).firestore();
  const old=doc(legacy,'users/dev-legacy');
  await assertSucceeds(setDoc(old,{displayName:'Legacy Student'}));
  await assertSucceeds(updateDoc(old,{yearLevel:2}));
  await assertFails(updateDoc(old,{isTester:true}));
 }finally{await env.cleanup();}
});
