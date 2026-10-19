const {test}=require('node:test');
const assert=require('node:assert/strict');
const {readFileSync}=require('node:fs');
const {initializeTestEnvironment,assertFails,assertSucceeds}=require('@firebase/rules-unit-testing');
const {doc,setDoc,updateDoc,getDoc,deleteDoc,deleteField,Timestamp,serverTimestamp,runTransaction}=require('firebase/firestore');

test('Heart refills are server-timed single debits with no reset or cross-account bypass',async()=>{
  const env=await initializeTestEnvironment({projectId:'demo-cyberity-spark',firestore:{host:'127.0.0.1',port:8088,
    rules:readFileSync(require('node:path').join(__dirname,'../firestore.rules'),'utf8')}});
  const auth={email_verified:true,auth_time:Math.floor(Date.now()/1000)};
  const db=env.authenticatedContext('heart-owner',auth).firestore();
  const ref=doc(db,'userProgress/heart-owner');
  async function allow(promise) {try {return await assertSucceeds(promise);} catch(error) {throw new Error('Valid heart operation rejected: ' + error.message);}}
  const debit=hearts=>({hearts,heartsUpdatedAt:serverTimestamp()});
  async function seed(hearts,elapsed) {
    await env.withSecurityRulesDisabled(ctx=>setDoc(doc(ctx.firestore(),ref.path),{
      hearts,heartsUpdatedAt:Timestamp.fromMillis(Date.now()-elapsed)
    }));
  }
  try {
    await assertFails(setDoc(ref,debit(5))); // Cannot initialize a stored full balance.
    await allow(setDoc(ref,debit(4))); // First mistake starts from five.
    await assertFails(updateDoc(ref,debit(5)));
    await assertFails(updateDoc(ref,debit(4))); // Resetting the timestamp cannot avoid a debit.
    await assertFails(updateDoc(ref,{hearts:5}));
    await assertFails(updateDoc(ref,{heartsUpdatedAt:deleteField(),hearts:deleteField()}));
    await assertFails(deleteDoc(ref));
    await allow(updateDoc(ref,debit(3)));
    await seed(0,899000);
    await assertFails(updateDoc(ref,debit(1)));
    await assertFails(updateDoc(ref,debit(0))); // No heart available to spend yet.
    await seed(0,901000);
    await allow(updateDoc(ref,debit(0))); // One earned, one spent.
    await seed(0,1801000);
    await assertFails(updateDoc(ref,debit(2))); // Cannot materialize refill without spending.
    await allow(updateDoc(ref,debit(1)));
    await seed(2,9001000);
    await assertFails(updateDoc(ref,debit(5)));
    await allow(updateDoc(ref,debit(4))); // Capped at five before debit.
    for (const shift of [-86400000,86400000]) {
      await assertFails(updateDoc(ref,{hearts:3,heartsUpdatedAt:Timestamp.fromMillis(Date.now()+shift)}));
      await assertFails(updateDoc(ref,{clockPing:Timestamp.fromMillis(Date.now()+shift)}));
    }
    const other=env.authenticatedContext('heart-other',auth).firestore();
    await assertFails(getDoc(doc(other,ref.path)));
    await assertFails(updateDoc(doc(other,ref.path),debit(3)));
    await seed(5,1000);
    async function spend(retries=2) {
      try {return await runTransaction(db,async tx=>{
        const current=await tx.get(ref);
        tx.set(ref,debit(current.get('hearts')-1),{merge:true});
      });} catch(error) {if(error.code === "permission-denied" && retries > 0) return spend(retries-1); throw error;}
    }
    try {await Promise.all([spend(),spend()]);} catch(error) {throw new Error("Concurrent debit failed: " + error.message);}
    assert.equal((await getDoc(ref)).get('hearts'),3,'Concurrent debits must both be charged');
    await allow(updateDoc(ref,{clockPing:serverTimestamp()}));
  } finally {await env.cleanup();}
});

test('Queued heart receipts retry once, remain private, and cannot forge a balance',async()=>{
  const env=await initializeTestEnvironment({projectId:'demo-cyberity-spark',firestore:{host:'127.0.0.1',port:8088,
    rules:readFileSync(require('node:path').join(__dirname,'../firestore.rules'),'utf8')}});
  const auth={email_verified:true,auth_time:Math.floor(Date.now()/1000)};
  const db=env.authenticatedContext('cached-heart-owner',auth).firestore();
  const parent=doc(db,'userProgress/cached-heart-owner');
  const id='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
  const receipt=doc(db,parent.path+'/heartDebits/'+id);
  async function spend(operation,retries=2) {
    const proof=doc(db,parent.path+'/heartDebits/'+operation);
    try {return await runTransaction(db,async tx=>{
      const state=await tx.get(parent), existing=await tx.get(proof);
      if(existing.exists()) return;
      const count=state.exists() ? state.get('hearts') : 5;
      if(count>0) tx.set(parent,{hearts:count-1,heartsUpdatedAt:serverTimestamp(),lastHeartDebit:operation},{merge:true});
      tx.set(proof,{createdAt:serverTimestamp(),heartsAfter:Math.max(0,count-1),exhausted:count===0});
    });} catch(error) {if(error.code==='permission-denied' && retries>0) return spend(operation,retries-1); throw error;}
  }
  try {
    await assertFails(setDoc(receipt,{createdAt:serverTimestamp(),heartsAfter:4,exhausted:false}));
    await assertFails(setDoc(parent,{hearts:4,heartsUpdatedAt:serverTimestamp(),lastHeartDebit:id}));
    await spend(id);
    assert.equal((await getDoc(parent)).get('hearts'),4);
    await spend(id); // Lost acknowledgement / app restart / reconnect.
    assert.equal((await getDoc(parent)).get('hearts'),4);
    await Promise.all([spend(id),spend(id)]);
    assert.equal((await getDoc(parent)).get('hearts'),4);
    await assertFails(updateDoc(receipt,{heartsAfter:5}));
    await assertFails(deleteDoc(receipt));
    const other=env.authenticatedContext('cached-heart-other',auth).firestore();
    await assertFails(getDoc(doc(other,receipt.path)));
    await assertFails(setDoc(doc(other,receipt.path),{createdAt:serverTimestamp(),heartsAfter:3,exhausted:false}));
    const exhausted='bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';
    await assertFails(setDoc(doc(db,parent.path+'/heartDebits/'+exhausted),{createdAt:serverTimestamp(),heartsAfter:0,exhausted:true}));
    await env.withSecurityRulesDisabled(ctx=>setDoc(doc(ctx.firestore(),parent.path),{hearts:0,heartsUpdatedAt:Timestamp.now()}));
    await spend(exhausted);
    assert.equal((await getDoc(parent)).get('hearts'),0);
    await spend(exhausted);
    assert.equal((await getDoc(parent)).get('hearts'),0);
  } finally {await env.cleanup();}
});
