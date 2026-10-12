// Configure provider metadata and register ONLY this phone's local debug token.
// No token is printed or persisted by this script. Enforcement is never changed.
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const root = process.argv[2]; const mode = process.argv[3] ?? 'inspect';
const auth = require(path.join(root, 'lib/auth.js'));
const { requireAuth } = require(path.join(root, 'lib/requireAuth.js'));
const { Client } = require(path.join(root, 'lib/apiv2.js'));
const app = 'projects/86708012515/apps/1:86708012515:android:de6d8a099755035a36ccd6';
let stage = 'authentication';
(async () => {
  await requireAuth({ project: 'cyberitycvsu', ...auth.getGlobalDefaultAccount() });
  const client = new Client({ urlPrefix: 'https://firebaseappcheck.googleapis.com', auth: true });
  if (mode === 'enable-api') {
    const usage = new Client({ urlPrefix: 'https://serviceusage.googleapis.com', auth: true });
    const response = await usage.post('/v1/projects/86708012515/services/firebaseappcheck.googleapis.com:enable', {});
    console.log(JSON.stringify({ appCheckApiEnableOperation: response.body.name }));
  }
  if (mode === 'configure-outside-play') {
    await client.patch(`/v1/${app}/playIntegrityConfig`, {
      name: `${app}/playIntegrityConfig`, tokenTtl: '3600s',
      appIntegrity: { allowUnrecognizedVersion: true },
      deviceIntegrity: { minDeviceRecognitionLevel: 'MEETS_DEVICE_INTEGRITY' },
      accountDetails: { requireLicensed: false }
    }, { queryParams: { updateMask: 'tokenTtl,appIntegrity,deviceIntegrity,accountDetails' } });
    console.log('Play Integrity provider metadata configured for APK distribution outside Google Play.');
  }
  if (mode === 'register-phone' || mode === 'verify-phone') {
    stage = 'phone log lookup';
    const adb = process.argv[4]; const serial = process.argv[5];
    const log = execFileSync(adb, ['-s', serial, 'logcat', '-d', '-s',
      'com.google.firebase.appcheck.debug.internal.DebugAppCheckProvider:D', 'DebugAppCheckProvider:D', '*:S'],
      { encoding: 'utf8', maxBuffer: 16*1024*1024 });
    const match = [...log.matchAll(/(?:Firebase App Check debug token:|Console for your project:|allow list.*?:)\s*([a-f0-9-]{36})/gi)].at(-1);
    if (!match) { stage = 'no debug token in phone logs'; throw new Error('No token'); }
    if (mode === 'register-phone') {
      stage = 'Firebase debug registration';
      const result = await client.post(`/v1/${app}/debugTokens`, {
        displayName: 'Justin development phone (2026-10-12)', token: match[1]
      });
      console.log(JSON.stringify({ registered: Boolean(result.body.name) }));
    } else {
      stage = 'development token exchange';
      const config = JSON.parse(require('node:fs').readFileSync(path.join(__dirname, '../app/google-services.json'), 'utf8'));
      const exchange = new Client({ urlPrefix: 'https://firebaseappcheck.googleapis.com', auth: false });
      const result = await exchange.post(`/v1/${app}:exchangeDebugToken`, { debugToken: match[1] },
        { headers: { 'X-Goog-Api-Key': config.client[0].api_key[0].current_key } });
      console.log(JSON.stringify({ tokenExchangeVerified: Boolean(result.body.token), ttl: result.body.ttl }));
    }
  }
  const response = await client.get(`/v1/${app}/debugTokens`);
  console.log(JSON.stringify({ debugTokens: (response.body.debugTokens ?? []).map(t => ({ displayName: t.displayName })) }));
  try {
    const response = await client.get(`/v1/${app}/playIntegrityConfig`);
    console.log(JSON.stringify({ playIntegrity: response.body }));
  } catch { console.log(JSON.stringify({ playIntegrity: 'Not configured or unavailable' })); }
  console.log('Service enforcement was not changed.');
})().catch(error => { console.error(`App Check setup failed at ${stage}; status ${error.status ?? error.statusCode ?? 'unavailable'}. No secret was printed.`); process.exitCode = 1; });
