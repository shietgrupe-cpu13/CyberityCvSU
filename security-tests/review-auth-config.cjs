// Read-only configuration review using the Firebase CLI's existing login.
// No credentials or user records are printed. Pass the installed firebase-tools directory.
const path = require('node:path');
const root = process.argv[2];
if (!root) throw new Error('Pass the installed firebase-tools directory');
const auth = require(path.join(root, 'lib/auth.js'));
const { requireAuth } = require(path.join(root, 'lib/requireAuth.js'));
const { Client } = require(path.join(root, 'lib/apiv2.js'));
(async () => {
  const account = auth.getGlobalDefaultAccount();
  if (!account) throw new Error('Firebase CLI login required');
  await requireAuth({ project: 'cyberitycvsu', ...account });
  const client = new Client({ urlPrefix: 'https://identitytoolkit.googleapis.com', auth: true });
  const { body } = await client.get('/admin/v2/projects/cyberitycvsu/config',
    { headers: { 'x-goog-user-project': 'cyberitycvsu' } });
  console.log(JSON.stringify({ project: 'cyberitycvsu',
    emailPasswordEnabled: body.signIn?.email?.enabled,
    emailPrivacyConfig: body.emailPrivacyConfig ?? null,
    passwordPolicyConfig: body.passwordPolicyConfig ?? null,
    recaptchaConfig: body.recaptchaConfig ?? null,
    mfaState: body.mfa?.state ?? null }, null, 2));
  try {
    const appCheck = new Client({ urlPrefix: 'https://firebaseappcheck.googleapis.com', auth: true });
    const response = await appCheck.get('/v1/projects/86708012515/services');
    console.log(JSON.stringify({ appCheckServices: response.body.services ?? [] }, null, 2));
  } catch {
    console.log(JSON.stringify({ appCheckServices: 'Not verified: API unavailable or access denied' }));
  }
})().catch(() => { console.error('Auth configuration review failed; check project access and CLI login.'); process.exitCode = 1; });
