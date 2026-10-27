# Student full-name lock

The full name used on certificates becomes immutable to the student when they
confirm entry into Unit 1, Level 1 (Inbox Triage, ID 101). Both Start and Read
Lesson entry points first show the saved name and explain that starting locks it.
Not yet returns to the learning path without changing the profile. The separate
Level 0 beginner walkthrough does not lock the name.

Settings > Student information explains the rule beforehand. After the lock,
first, middle and last name fields are disabled. Year level and username remain
editable under their existing rules. Administrator correction through trusted
Firebase tools remains available for spelling mistakes.

## Enforcement and existing accounts

The private users/{uid}.fullNameLockedAt timestamp is assigned with a server
timestamp in a transaction before entering Level 101. The transaction compares
the confirmed full name with the current server profile; a concurrent name change
requires a fresh review. Transactions updating student names read the same profile
and progress documents. Starting requires connectivity; failures do not enter
the level or silently accept a cached identity.

Firestore rules preserve the timestamp and all four name fields after locking.
Year-level, username and onboarding updates retain existing authorization. Whole
document replacement cannot remove the lock. getAfter on progress also rejects
a batch that completes Level 101 and changes the name at the same time.

Existing completion of numeric Level 101 counts as locked even without the new
timestamp. Older versions did not record unfinished starts, so those cannot be
reconstructed. A learner who started but never completed Level 101 on an old APK
is prompted on their first entry using the updated APK. Existing locked profiles
without correct name details require administrator correction.

Profile exports already include the full private profile, and existing account
deletion removes that document; no extra collections or public identity fields
were added.

## Settings layout

Settings menu descriptions now sit below their titles within a weighted text
column. Icons and arrows retain bounded widths. Rows grow with wrapped text
instead of squeezing titles between a long trailing description and the arrow.
Profile badges wrap onto another line when space is limited.

## Validation

- Android debug build, unit tests and Android test compilation passed.
- All 13 Firestore emulator test cases passed, including new name-lock bypass,
  marker/type/overwrite, unauthorized access, legacy completion, combined-batch,
  oversized-name and allowed year/username/onboarding update checks.
- Cloud rules compilation dry run passed before deployment.
- All three Android tests passed on a temporary read-only Pixel 8 Pro emulator:
  explicit confirmation/cancel, disabled actions during a committing start, and
  non-overlapping Settings titles/descriptions at 280 dp width with normal,
  150% and 200% text scaling. Saved renders were visually inspected.
- Updated rules compiled and were deployed successfully to cyberitycvsu.
- Version 1.0.8 (build 9) is prepared locally; GitHub release publication is pending.
