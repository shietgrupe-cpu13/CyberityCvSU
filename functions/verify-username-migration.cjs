const fs = require('node:fs');
const assert = require('node:assert/strict');
const { getAuth } = require('firebase-admin/auth');
const { planUsernames } = require('./username-migration-policy.cjs');
const db = require('./username-migration-connection.cjs').connect();
(async()=>{
 const backupPath=process.argv[process.argv.indexOf('--backup')+1];
 assert.ok(process.argv.includes('--backup'), 'Provide the pre-migration backup.');
 const backup=JSON.parse(fs.readFileSync(backupPath,'utf8'));
 assert.equal(backup.project,'cyberitycvsu');
 const users=await db.collection('users').get();
 const registered=new Map(), missingAuth=new Set();
 for(let i=0;i<users.docs.length;i+=100){
  const result=await getAuth().getUsers(users.docs.slice(i,i+100).map(d=>({uid:d.id})));
  result.users.forEach(u=>registered.set(u.uid,Date.parse(u.metadata.creationTime)));
  result.notFound.forEach(u=>missingAuth.add(u.uid));
 }
 const plan=planUsernames(users.docs.filter(d=>!missingAuth.has(d.id)).map(d=>({uid:d.id,displayName:d.get('displayName'),registeredAt:registered.get(d.id)})));
 let repaired=0;
 for(const group of plan){
  const claim=await db.collection('usernames').doc(group.key).get();
  assert.equal(claim.get('uid'),group.winner.uid,'Earliest registration must own the claim.');
  for(const loser of group.losers){
   const doc=await db.collection('users').doc(loser.uid).get();
   assert.equal(doc.get('usernameChangeRequired'),true);
   assert.equal(doc.get('usernameKey'),undefined);
   assert.equal((await db.collection('leaderboard').doc(loser.uid).get()).exists,false);
   repaired++;
  }
 }
 for(const doc of users.docs){
  const original=backup.records.find(r=>r.path===doc.ref.path);
  assert.ok(original,'Profile appeared after the migration snapshot; audit again.');
  const current=JSON.parse(JSON.stringify(doc.data()));const before={...original.data};
  if(missingAuth.has(doc.id)) { assert.deepEqual(current,before,'Orphaned profiles must remain entirely unchanged.'); continue; }
  for(const field of ['displayName','usernameKey','usernameChangeRequired']){delete current[field];delete before[field];}
  assert.deepEqual(current,before,'Preserve all other profile fields.');
 }
 console.log(JSON.stringify({verified:true,activeAccounts:registered.size,reservedUsernames:plan.length,requiredUsernameChanges:repaired,orphanProfilesUntouched:missingAuth.size,profileFieldsPreserved:true}));
 await db.terminate();
})().catch(e=>{console.error(e.message);process.exitCode=1;});
