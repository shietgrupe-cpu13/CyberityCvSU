// Read-only aggregate compatibility audit. Never prints IDs or individual records.
const path = require('node:path');
const root = process.argv[2];
const auth = require(path.join(root, 'lib/auth.js'));
const { requireAuth } = require(path.join(root, 'lib/requireAuth.js'));
const { Client } = require(path.join(root, 'lib/apiv2.js'));
const ids = new Set([101,102,103,104,105,106,200,201,202,203,204,205,250,301,302,303,304,305,350,401,402,403,404,405,450,501,502,503,504,505,550,601,602,603,604,605]);
(async () => {
  await requireAuth({ project: 'cyberitycvsu', ...auth.getGlobalDefaultAccount() });
  const client = new Client({ urlPrefix: 'https://firestore.googleapis.com', auth: true });
  const report = { records: 0, invalidAwards: 0, invalidCompletions: 0, invalidSpending: 0, fields: [] };
  const fields = new Set(); let page = '';
  do {
    const response = await client.get('/v1/projects/cyberitycvsu/databases/(default)/documents/userProgress',
      { queryParams: { pageSize: 100, ...(page ? { pageToken: page } : {}) } });
    for (const record of response.body.documents ?? []) {
      report.records++; const data = record.fields ?? {}; Object.keys(data).forEach(k => fields.add(k));
      const awards = data.levelXp?.mapValue?.fields ?? {}; let earned = 0;
      for (const [key, value] of Object.entries(awards)) {
        const amount = Number(value.integerValue); const cap = Number(key) === 200 ? 0 : 150;
        if (!ids.has(Number(key)) || !Number.isInteger(amount) || amount < 0 || amount > cap) report.invalidAwards++;
        else earned += amount;
      }
      const completed = (data.completedLevels?.arrayValue?.values ?? []).map(v => Number(v.integerValue));
      if (completed.some(id => !ids.has(id)) || new Set(completed).size !== completed.length ||
          Object.keys(awards).some(key => !completed.includes(Number(key)))) report.invalidCompletions++;
      const spent = Number(data.xpSpent?.integerValue ?? 0);
      if (!Number.isInteger(spent) || spent < 0 || spent > 50 + earned) report.invalidSpending++;
    }
    page = response.body.nextPageToken ?? '';
  } while (page);
  report.fields = [...fields].sort(); console.log(JSON.stringify(report, null, 2));
})().catch(() => { console.error('Score audit failed; no data changed.'); process.exitCode = 1; });
