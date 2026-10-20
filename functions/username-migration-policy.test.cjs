const {test}=require('node:test');const assert=require('node:assert/strict');
const {planUsernames}=require('./username-migration-policy.cjs');
test('Earliest Firebase registration wins regardless of input order or capitalization',()=>{
 const plan=planUsernames([{uid:'later',displayName:' ALICE ',registeredAt:20},{uid:'first',displayName:'Alice',registeredAt:10},{uid:'third',displayName:'alice',registeredAt:30}]);
 assert.equal(plan[0].winner.uid,'first');assert.deepEqual(plan[0].losers.map(p=>p.uid),['later','third']);assert.equal(plan[0].key,'alice');
});
test('Missing or tied registration dates stop instead of choosing an arbitrary winner',()=>{
 assert.throws(()=>planUsernames([{uid:'a',displayName:'Alice'}]));
 assert.throws(()=>planUsernames([{uid:'a',displayName:'Alice',registeredAt:10},{uid:'b',displayName:'ALICE',registeredAt:10}]));
});
