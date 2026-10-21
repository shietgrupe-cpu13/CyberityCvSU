const { initializeApp, applicationDefault, refreshToken } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
exports.connect = () => {
// Optionally reuse the operator's existing CLI OAuth session in memory.
// No token, service-account key, or credential file is written by this script.
const cliIndex = process.argv.indexOf('--firebase-cli-path');
let db;
if (cliIndex >= 0) {
 const path = require('node:path');
 const cliRoot = process.argv[cliIndex + 1];
 if (!cliRoot) throw Error('Provide the installed firebase-tools directory.');
 const cliAuth = require(path.join(cliRoot, 'lib/auth.js'));
 const cliApi = require(path.join(cliRoot, 'lib/api.js'));
 const account = cliAuth.getProjectDefaultAccount(process.cwd());
 if (!account?.tokens?.refresh_token) throw Error('Sign into Firebase CLI first.');
 const credentials = {type:'authorized_user',client_id:cliApi.clientId(),client_secret:cliApi.clientSecret(),refresh_token:account.tokens.refresh_token};
 initializeApp({credential:refreshToken(credentials),projectId:'cyberitycvsu'});
 db = new (require('@google-cloud/firestore').Firestore)({projectId:'cyberitycvsu',credentials});
} else {
 initializeApp({ credential: applicationDefault(), projectId: 'cyberitycvsu' });
 db = getFirestore();
}
return db;
};
