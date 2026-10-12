# XP integrity and App Check — 12 October 2026

## Score changes on Spark

The leaderboard publisher now reads profile, privacy and progress inside a
Firestore transaction. It derives the public balance as 50 starting XP plus
recorded awards minus spent hints, floored at zero. It does not accept a score
from the UI. Privacy's show-leaderboard transaction uses the same calculation;
this also fixes its previous omission of the starting grant.

The rules independently require public XP, display name and tester status to
match the after-state private records. Public timestamps must be server timestamps.
Progress accepts known curriculum IDs only; each first award is an integer from
0 to 150 (the actual grading maximum), with matching completion. The walkthrough
200 has a zero cap. Only one new award can be recorded per write. Existing awards
and completions cannot be removed or revised. Hint spending cannot decrease or
exceed available funds; increments are 15, 30 or 50. Heart values are bounded
0–5; changed heart/clock timestamps must be server timestamps.

Progress/profile deletion is permitted through verified account deletion, not
as a way to reset hint costs or clear a tester flag. A DEV profile cannot switch
off its tester status. Existing privacy, cutoff, session and deletion recovery
protections remain. Rules use after-state reads so legitimate atomic updates work.

The read-only compatibility audit saw six progress records with no invalid awards,
completion lists or spending. It reported aggregate counts only and did not modify
student progress. Historical fields can remain unchanged, but arbitrary new fields
cannot be introduced into progress. clockPing is included for ServerClock syncing.

**Limit:** these remain bounded client-reported achievements. A modified client
can still claim a plausible first completion/award, or fail to report a hint view.
App Check does not establish correctness of answers. Trusted competitive grading
requires a server that verifies submissions and owns the score, beyond this
Spark-only implementation. Do not advertise these scores as cheat-proof.

When adding curriculum IDs or changing actual grading, update ScorePolicy and
the matching rule catalog/caps together and run the tests. Old clients which
reconstruct visibility without the starting grant need the updated APK.

## App Check integration

CyberityApplication initializes App Check before activity code uses Auth/Firestore.
Release builds compile only the Play Integrity provider setup. Debug builds use
the development provider and its dependency is debug-only; production builds
contain no development provider setup or embedded development secret.

Firebase App Check API was enabled. The connected development phone's SDK-generated
secret was registered privately under the name `Justin development phone (2026-10-12)`.
Keep development builds/tokens private and revoke this registration when no longer
needed. The setup script reads the SDK log without printing the secret or JWT.

For APK distribution outside Google Play, provider metadata now allows unrecognized
app versions and does not require a Play license, while requiring
MEETS_DEVICE_INTEGRITY and using a 1-hour token TTL. This follows Firebase's
off-Play distribution guidance. It provides a weaker app-recognition signal than
Play-distributed builds; keep signing-key ownership and SHA-256 registration part
of the release process.

Authentication and Firestore enforcement remain UNENFORCED. This is intentional:
existing groupmate APKs and unregistered production builds must not be locked out.
No registered signing SHA certificates were found during review. The release
signing certificate must be registered, a signed release must pass real Play
Integrity attestation, and request metrics for distributed clients must be checked
before enabling service enforcement. A development-token test bypasses attestation
and is not evidence that a release APK passes Play Integrity.

The first production-metadata change was rejected by automatic approval review;
the user subsequently delegated the choice with the requirement to preserve
usability. The reviewed retry succeeded with enforcement left off.

## Validation

- Debug app and Android test APK build; release provider Kotlin compilation.
- Three ScorePolicy unit tests: catalog consistency, bounds and starting/hint balance.
- Three emulator suites: forged scores/rewards/refunds/role changes, full-history
  rule cost, session cutoff, privacy visibility and account deletion.
- Phone App Check token verification and signed-in server reads passed after API
  enablement and private development registration. Initial API-disabled and device
  timeout failures were diagnosed; waking the phone allowed the checks to finish.
- Final phone run: all three AppCheckSmokeTest tests passed in 3.408 seconds,
  including normal leaderboard publication from stored profile/progress and its
  resulting XP/name/tester values. Both final APKs were installed successfully.
- Rule changes were deployed to cyberitycvsu. No billing upgrade or Cloud Functions
  deployment was performed. Real student records were not used for attack writes;
  attack cases ran only in the emulator.

## Maintenance tools

- security-tests/audit-score-data.cjs: read-only aggregate compatibility review.
- security-tests/setup-app-check.cjs: inspect, enable-api, register-phone,
  verify-phone, configure-outside-play. There is no enforcement mutation mode.
- security-tests/scores.test.cjs and app ScorePolicyTest/AppCheckSmokeTest.

## Primary documentation

- https://firebase.google.com/docs/app-check/android/play-integrity-provider
- https://firebase.google.com/docs/app-check/android/debug-provider
- https://firebase.google.com/docs/reference/appcheck/rest/v1/projects.apps.playIntegrityConfig
- https://firebase.google.com/docs/firestore/security/rules-conditions
