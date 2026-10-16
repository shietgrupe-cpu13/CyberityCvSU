# Account security on Firebase Spark

The Android app uses Firebase Authentication and Firestore directly. No Cloud
Functions, Blaze upgrade, Firebase Functions SDK, or TTL policy is needed.
The old functions/ backend is retained as an unused Blaze alternative; do not
run the old functions deployment command for this Spark implementation.

## Deployment

The existing rules supplied by the project owner were merged into the root
firestore.rules file, keeping profile/progress ownership, student ID permissions,
and leaderboard validation. Every existing allow now checks sparkSessionActive().
The combined rules passed emulator tests and Firebase compilation and were
published to cyberitycvsu on 2026-10-04 using:
firebase deploy --only firestore:rules --project cyberitycvsu

Install the updated app on both test devices. Records are created automatically
under accountSecuritySpark/{uid}/sessions/{sessionId}. Device testing is still
required. Future rule changes should edit firestore.rules and run local tests
before deploying. docs/account-security-spark.rules remains a reference fragment.

## Behavior and limits

The list shows up to 50 recently tracked records, hiding signed-out, revoked,
and more-than-90-day-old activity. Device descriptions are client reported.
Records are not automatically deleted on Spark; there is no TTL retention
promise. Reads/writes count against Spark quotas. The app checks server metadata
once per minute while account screens are open and on startup. Failed/offline
checks are not proof of revocation. Cached content cannot be remotely erased.

All-device logout requires password reauthentication and the Firebase MFA
challenge when enrolled. Rules require authentication within five minutes and
use a server timestamp cutoff. Older sign-ins cannot remove, backdate, or change
that cutoff. Refreshing an ID token does not change its original auth_time;
signing in again does. Same-second sign-ins may need retrying the next second.

This blocks protected Firestore access from older sign-ins, including old app
versions when rules cover their resources. It does not revoke Firebase Auth
refresh tokens or automatically protect Storage/other backends. Those need
separate enforcement. The current device signs out after the cutoff is committed;
connected devices return to login on the next check. Older app versions may show
access errors instead of navigating to login.

Selective remote logout and keeping the initiating device signed in are not
implemented. App-generated IDs are not bound to Firebase tokens and cannot
securely enforce per-device revocation against a modified client.

## Verification after publishing

Use a verified test account and two updated devices. Check the device list,
password/MFA verification, all-device logout, restart, and offline/reconnect.
Verify older ID tokens cannot read/write profiles/progress, touch a session,
change/delete the cutoff, or access another account's metadata. Sign in again
and confirm access returns. Incorrect credentials and unavailable Firestore
must not show successful remote logout. Preserve existing profile/leaderboard
permissions and checks.

## Local validation

The Android Kotlin compilation passed. The Firestore emulator tests verify the
combined production rules against all four existing collections, including cross-account
access, forged timestamps/auth_time, stale logout requests, cutoff tampering,
same-second sign-ins, and fresh sign-in recovery. The combined production rules
were also compiled successfully and published by the Firebase CLI. Two-device
app verification is still required.

To repeat: in security-tests, run npm ci, then (with Java 21+ on PATH):
firebase emulators:exec --only firestore --project demo-cyberity-spark --config firebase.json "npm test"

References:
- https://firebase.google.com/docs/firestore/security/rules-conditions
- https://firebase.google.com/docs/reference/rules/rules.firestore.Request
- https://firebase.google.com/docs/auth/admin/manage-sessions
