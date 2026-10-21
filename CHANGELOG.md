# Changelog

## 1.0.4 — 21 October 2026

- Added Check for updates in Settings > Help & About, using the latest published GitHub release.
- Newer versions offer Download and Cancel. Canceling does not prevent checking again; current versions show “You’re up to date.” Connection failures offer a retry through the same button.
- Added unique username changes in Settings > Student information, with a 30-day wait after each successful change.
- Enforced the cooldown using server timestamps in Firestore rules; users cannot reset it by editing their phone clock or deleting the timestamp.
- Required duplicate-name repairs remain possible during a cooldown and start a fresh 30-day wait after repair.
- Renames preserve full names and learning progress, refresh the leaderboard, and release the old username.
- Version 1.0.4, build 5. Firestore rules compiled and deployed successfully to cyberitycvsu on 21 October 2026. The existing-account migration completed on 21 October 2026: three usernames reserved, one later duplicate flagged, and two orphaned profiles left unchanged.


## 1.0.3 — 20 October 2026

- Split student names into required first and last names plus an optional middle name in registration, profile completion, and settings.
- Students without a middle name can leave it blank.
- Save the three name fields and combined full name in the private database profile; certificates combine only the provided fields.
- Preserve existing full-name-only records without guessing how to split them.
- Added name validation and database tests for missing first/last names, optional middle names, and matching certificate names.
- Version 1.0.3, build 4. Backend rules deployment and username migration remain pending as described in v1.0.2.


## 1.0.2 — 20 October 2026

### Student information and certificates

- Added a separate full-name field to registration and profile completion, saved in the private user profile.
- Existing students can add or correct their full name in Settings > Student information.
- Completion certificates use the full name instead of the leaderboard username. A valid full name is required before downloading a certificate or developer sample.

### Unique usernames

- Reserved usernames atomically with account profiles, ignoring capitalization and surrounding spaces.
- Added specific username-taken feedback, including simultaneous registration collisions.
- Added an administrator migration that uses Firebase Authentication registration dates: the earliest registered account keeps the username; later duplicate accounts must choose another.
- Prefilled existing student information during username repair and preserved onboarding and learning progress.
- Included username claims in account data export and deletion.

### Distribution and validation

- Updated the app to version 1.0.2, build 3, using the existing debug signing key.
- Android compilation and certificate unit tests passed; all 12 Firestore emulator checks and 2 migration ordering tests passed.
- Live rollout requires the updated Firestore rules and username migration. These backend steps have not been applied because Firebase sign-in must be restored. Until then, uniqueness and existing-duplicate resolution are not active on the live database.


## 1.0.1 — 19 October 2026

### Learning simulations

- Reworked Unit 02 into focused practical steps across connected cases.
- Added fictional password entry, confirmation, saving, and sign-in tests for credential repairs.
- Clarified Strong Passwords case 3 with sample passwords, separate policy-test results, and completion guidance.
- Kept the MFA training proof visible beside the proof-entry field.
- Preserved case navigation after completion and added storage fallback for connected tasks when WebView session storage is unavailable.
- Added Unit 05 follow-up problems, saved problem progress, and Wi-Fi passphrase confirmation.
- Removed unused Unit 02 sorting content and duplicate instructions and results in Units 02 and 05.

### Hearts

- Added per-account cached heart state and queued deductions with duplicate protection.
- Set the refill interval to 15 minutes with server-side timing validation.
- Enabled unlimited hearts for server-confirmed tester accounts while preserving their normal balance for revocation.

### Distribution and validation

- Updated the Android app to version 1.0.1, build 2.
- Added permanent latest-release download and checksum links to the README.
- Passed 55 simulation checks and Android unit checks; built the debug APK successfully.
- Added automated progression checks using visible, enabled controls across practical Unit 02 cases and all 18 Unit 05 tasks. Device testing remains separate.

The downloadable APK uses the current Android debug signing key. The original v1.0 release remains available in the release history.

## 1.0

Initial published release with six cybersecurity learning units, lessons, quizzes, interactive simulations, progress and EXP, leaderboards, student profiles, learning streaks, and PDF completion certificates. Earlier v1.0 download updates included completion-save recovery improvements.
