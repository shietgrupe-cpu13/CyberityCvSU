# Student information and completion certificates

Implemented locally on unit_06_levels on 16 October 2026. Not committed, pushed,
installed on a phone, or deployed to Firebase in this change.
Later that day, the tested year validation rule was deployed with the developer
access rules. The application source still requires the updated APK; see
NEXT_SESSION.md for current deployment status.

## Student information

- Registration and incomplete-profile recovery require a year selection:
  first through fourth year, or fifth year and higher.
- Year is stored as optional integer yearLevel (1-5) on users/{uid}. Existing
  profiles without it remain complete for login; no re-registration is required.
- Settings > Student information shows the existing username and student ID,
  with a year selector and acknowledged save action. The update changes only
  yearLevel and updatedAt, preserving identity, progress and EXP.
- The Profile tab displays year level and observes changes. The private profile
  cache includes year level and removes it during local account-deletion cleanup.
- Year is included automatically in the existing private-profile JSON export.
  Neither year nor student ID is published to the leaderboard.
- New rules validate a present year as an integer in 1-5 while accepting missing
  fields on older profiles. This validation change is local; previously deployed
  rules already permit the additional profile field but do not validate its year
  range. No cloud mutation was performed in this task.

## Certificate

Settings > Completion certificate fetches the profile and progress from the
server. The preview and downloaded one-page landscape PDF use the saved display
name (the registration username), with student ID and year directly underneath.
No identity fields are entered again on the certificate screen.

Download requires complete student identity, a valid year level, and every
required course level. Only skippable walkthrough 200 and REWARD-type nodes are
excluded. Developer-unlock mode does not satisfy completion. An unfinished
certificate is marked as a preview and cannot be downloaded through this flow.
Developer-access accounts may now download that marked preview for testing even
without course completion/year level. It retains a large PREVIEW watermark and
non-completion footer. Earned certificates retain all normal requirements.

The document includes completion/thank-you wording, issue date, a stable
account/course certificate number, and three blank developer signature spaces.
CertificateBranding in CertificatePolicy.kt holds the three names and common
role for a future template update. No signatures or endorsements were invented.
Signature-image upload and a team certificate-design editor are not included.

The issue date is the export date. Certificate number is a deterministic
account/course reference, not a public verification endpoint or an independently
verified qualification. Existing client-reported completion integrity limitations
remain. This implementation does not add a server certificate issuance registry.

## Validation

- Debug APK build passed; all 43 JVM tests passed.
- Six Firestore emulator suites passed, including year validation on both create
  and update, legacy compatibility, and cross-account privacy.
- Added a dropdown instrumentation test and compiled Android test sources;
  instrumentation/phone checks have not been run because phone testing was paused.
- Five new JVM checks cover legacy profiles, certificate eligibility and missing
  levels, invalid years, stable certificate identifiers and year parsing.
- Corrected stale local LearnScreen references to the existing Unit 3 simulations
  and restored current Unit 5/6 routes/types from HEAD. Also restored a missing
  saving-revision state declaration needed by the local saving integration.
  Other pre-existing local learning-screen edits were preserved.

Pending device checks: registration with no year selected; selecting/saving year;
legacy user sign-in and update; profile refresh; certificate field placement,
three signature spaces, preview/download lock, export cancellation and PDF layout.

## Full names and unique usernames (20 October 2026)

Registration and profile completion collect a full name separately from the leaderboard username. Existing accounts can add or correct their full name in Settings > Student information. Full name validation accepts 2–100 characters and rejects control characters. Certificate previews and PDFs use the full name; downloads (including developer samples) require a valid full name. Missing full names do not block login or learning.

New profiles reserve a lowercase, trimmed username in usernames/{usernameKey} in the same transaction as users/{uid} and studentIds/{id}. Collisions return a specific error, including when rules reject a competing commit. Username claims contain only uid; full names remain in the private profile. Account export and deletion include username claims.

Before rollout, pause registration/profile writes, run node functions/migrate-usernames.cjs with Application Default Credentials for cyberitycvsu, review the report, then run with --apply to reserve each username for the earliest Firebase Authentication registration. Later duplicate accounts are marked usernameChangeRequired and removed from the leaderboard until they choose a unique username. Missing registration dates, tied earliest dates, and invalid names stop the audit for explicit resolution. The script never silently renames accounts. Publish the updated rules before distributing the new app and require old app versions to update for registration. Without this backfill, unclaimed legacy names cannot be protected from new registrations.

Live database inspection/deployment was unavailable because the saved Firebase CLI login has expired. No live account records or rules were changed. Android compilation and certificate unit tests pass; all 12 emulator tests passed, including simultaneous username claims and legacy full-name updates.

Duplicate ownership uses Firebase Auth metadata.creationTime, not profile updatedAt or document order. The repair screen loads the existing student ID, full name and year level; successful repair preserves onboarding and all learning progress. Rules prevent clearing usernameChangeRequired without owning a matching username claim. Full names are saved in users/{uid}.fullName during registration and settings updates, and are not published on the leaderboard.

Name fields now use required firstName and lastName plus optional middleName. Registration, profile completion and Settings all accept a blank middle name. Saves write the trimmed components and composed fullName (first, optional middle, last) together. Legacy fullName-only records remain valid and continue to appear on certificates; the app does not guess how to split existing names.

Username changes are available in Settings > Student information. The first voluntary change is available immediately; each successful change starts a 30-day wait using usernameChangedAt and server request time. Unchanged-name writes cannot alter or remove this timestamp. Required duplicate repairs bypass an existing wait and start a new one on repair. A rename atomically reserves the new name, updates the profile, releases the owned old claim, and removes the stale leaderboard row; normal leaderboard publication then restores it with the new name and existing score. Live rules deployment remains required.
