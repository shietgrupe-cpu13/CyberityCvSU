// Audit by default. Pause registration/profile writes during --apply.
const { FieldValue } = require('firebase-admin/firestore');
const { getAuth } = require('firebase-admin/auth');
const { planUsernames } = require('./username-migration-policy.cjs');
const db = require('./username-migration-connection.cjs').connect();
(async () => {
 const users = await db.collection('users').get(), registered = new Map(), missingAuth = new Set();
 for (let i=0;i<users.docs.length;i+=100) {
  const result = await getAuth().getUsers(users.docs.slice(i,i+100).map(d=>({uid:d.id})));
  for (const missing of result.notFound) if (missing.uid) missingAuth.add(missing.uid);
  for (const user of result.users) registered.set(user.uid, Date.parse(user.metadata.creationTime));
 }
 const backupIndex = process.argv.indexOf('--backup');
 if (process.argv.includes('--apply') && backupIndex < 0) throw Error('Use --backup <local-file> before applying.');
 if (backupIndex >= 0) {
  const fs = require('node:fs');
  const claimDocs = await db.collection('usernames').get();
  const boardDocs = await db.collection('leaderboard').get();
  const records = [...users.docs,...claimDocs.docs,...boardDocs.docs].map(d=>({path:d.ref.path,data:d.data()}));
  fs.writeFileSync(process.argv[backupIndex+1],JSON.stringify({project:'cyberitycvsu',createdAt:new Date().toISOString(),records},null,2),{flag:'wx'});
 }
 console.log(JSON.stringify({profileCount:users.size,deletedAuthProfilesSkipped:[...missingAuth]}));
 const plan = planUsernames(users.docs.filter(d=>!missingAuth.has(d.id)).map(d=>({uid:d.id, displayName:d.get('displayName'), registeredAt:registered.get(d.id)})));
 console.log(JSON.stringify(plan.map(p=>({username:p.key,keepsUsername:p.winner.uid,mustChange:p.losers.map(p=>p.uid)})),null,2));
 if (!process.argv.includes('--apply')) { console.log('Audit only. --apply reserves the earliest registered owner and flags later accounts for a username change.'); return; }
 for (const group of plan) {
  // Two writes per member; stay under Firestore transaction limits.
  if (group.losers.length > 200) throw Error('Too many duplicates; resolve this group separately.');
  await db.runTransaction(async tx => {
   const members=[group.winner,...group.losers];
   const snapshots=await Promise.all(members.map(p=>tx.get(db.collection('users').doc(p.uid))));
   const claim=db.collection('usernames').doc(group.key);
   const owner=await tx.get(claim);
   snapshots.forEach((doc,i)=>{
    if(!doc.exists || doc.get('displayName')!==members[i].displayName) throw Error('Profile changed; rerun the audit.');
   });
   if(owner.exists && !members.some(p=>p.uid===owner.get('uid'))) throw Error('Unexpected username owner; rerun audit.');
   tx.set(claim,{uid:group.winner.uid});
   tx.update(snapshots[0].ref,{displayName:group.winner.displayName.trim(),usernameKey:group.key,usernameChangeRequired:false});
   for (const doc of snapshots.slice(1)) {
    tx.update(doc.ref,{usernameKey:FieldValue.delete(),usernameChangeRequired:true});
    tx.delete(db.collection('leaderboard').doc(doc.id));
   }
  });
 }
 console.log('Reserved ' + plan.length + ' usernames. Later duplicate accounts must choose another.');
})().catch(error=>{console.error(error.message);process.exitCode=1;});
