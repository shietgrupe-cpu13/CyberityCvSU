# Developer mode uses the existing isTester field

The app now uses users/{Firebase Auth UID}.isTester as requested by the team.
The separate developerAccess collection is no longer used. No records were
created in that collection during the earlier implementation.

## Toggle in Firebase Console

1. Open cyberitycvsu > Firestore Database > Data > users.
2. Open the developer account's document. Its ID is the Firebase Auth UID.
3. Set isTester (boolean) to true to enable developer mode; false disables it.
4. Repeat for each authorized team account. Updated APKs listen for changes;
   old APKs with the hardcoded switch cannot be controlled remotely.

The verified account Justinepugeh was enabled on 16 October 2026 after explicit
user authorization. Its existing leaderboard tester marker was synchronized.
No completed levels, EXP, hearts or credentials were changed.

## What it controls

- Unlocks level navigation without marking levels complete.
- Allows developer sample-PDF download before course completion or year selection.
  Samples retain a large PREVIEW watermark, non-completion footer and PREVIEW
  filename. Developer status is rechecked from the server after file selection.
- Excludes the account from the student leaderboard through the existing tester
  marker. The profile listener republishes this status when the app is running.
- Does not grant free hints, refill hearts, or bypass earned-certificate rules.
- Missing/non-boolean/cached/pending/error values default to normal progression.
- Turning it off closes a pending level preview and rechecks launch access. A
  level already running is allowed to finish rather than discard work.

## Administrator ownership

Client profile creation can set isTester only to false or omit it. Updates must
preserve the previous value. Profile saving reads and retains the current server
flag, so year/name updates cannot accidentally overwrite an administrator's flag.
A DEV-prefixed student ID no longer automatically grants tester privileges.
Administrators assign or remove the flag in Console or through a trusted Admin
SDK. This protection is necessary because isTester now grants developer features.
Old debug APKs that try to register DEV IDs with isTester=true need updating;
ordinary non-tester registration remains compatible.

The flag remains in the normal private user profile, so existing profile export
and account deletion already cover it. The separate developerAccess rules and
privacy additions were removed.

## Validation and rollout

- Debug build and all 49 JVM tests passed.
- Seven local Firestore suites passed, including self-grant prevention, preserved
  tester status during profile updates, administrator changes and legacy users.
- The updated rules compiled and were released to cloud.firestore successfully.
- A read-only phone diagnostic identified the signed-in UID through FirebaseAuth
  without printing tokens. Only the diagnostic test APK was installed; the main
  Cyberity APK and its data were retained. One phone probe passed.
- The current user's isTester=true was verified through a fresh server read.
- The user subsequently reported the features working. The implementation is
  being committed on unit_06_levels at the user's request; master is unchanged.
