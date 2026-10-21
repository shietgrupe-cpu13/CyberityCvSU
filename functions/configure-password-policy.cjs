const fs = require('node:fs');
const {getAuth} = require('firebase-admin/auth');
require('./username-migration-connection.cjs').connect();
(async()=>{
 const manager=getAuth().projectConfigManager();
 const previous=await manager.getProjectConfig();
 if(!process.argv.includes('--apply')) {console.log(JSON.stringify({passwordPolicyConfig:previous.passwordPolicyConfig}));return;}
 const backupIndex=process.argv.indexOf('--backup');
 if(backupIndex<0)throw Error('Provide --backup <new-local-file>.');
 fs.writeFileSync(process.argv[backupIndex+1],JSON.stringify({passwordPolicyConfig:previous.passwordPolicyConfig??null},null,2),{flag:'wx'});
 const policy={enforcementState:'ENFORCE',forceUpgradeOnSignin:false,constraints:{minLength:8,maxLength:4096,requireUppercase:true,requireLowercase:true,requireNumeric:true,requireNonAlphanumeric:true}};
 await manager.updateProjectConfig({passwordPolicyConfig:policy});
 const verified=await manager.getProjectConfig();
 console.log(JSON.stringify({passwordPolicyConfig:verified.passwordPolicyConfig}));
})().catch(e=>{console.error(e.message);process.exitCode=1;});
