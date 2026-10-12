# Cyberity: resume here

Last updated: 2026-10-12.

## Purpose

Score/App Check update: bounded first-clear XP rules deployed; leaderboard now
derives its balance from server-stored progress/profile and includes starting XP.
Replay award changes, refunds and tester-flag clearing are blocked. API and phone
development registration are set up; release uses Play Integrity. Outside-Play
provider metadata is configured with device integrity required; service enforcement
remains OFF to keep existing APKs usable. No signing SHA certificates are registered.
Before enforcement: register the release signing certificate, test a signed release
and monitor distributed clients. See docs/score-integrity-and-app-check.md. Changes
are uncommitted. These scores are bounded client reports, not server-graded proof.
Final validation passed: three ScorePolicy unit tests, three emulator rule suites,
debug build, release-provider compilation, and three phone AppCheckSmokeTest checks
(token, server reads, matching leaderboard publication). Final app is installed.

Unit 02 / Unit 05 lesson update: all twelve levels now have task-linked lessons
using the existing lesson screen, with 36 preparation pages, 24 no-stakes questions
and clickable primary references. Source provenance and claim checks are documented
in docs/lesson-source-review.md. LessonContentTest covers both units alongside
existing Unit 01/06 lessons. Debug build and both coverage/question tests passed.
Changes are uncommitted; do not overwrite the earlier login-protection changes.

October 12 login protection update: elapsed-time cooldown with boot-aware persisted
restoration implemented; debug build and all 12 AuthThrottleTest tests passed.
Live Firebase review confirms enumeration protection and enforced password policy;
Authentication/Firestore App Check are UNENFORCED, with no reCAPTCHA config returned.
No cloud settings changed. See docs/login-protection.md. Next: real-device cooldown
checks and staged App Check planning before any enforcement. Changes are uncommitted.

Continue improving Cyberity while learning software development and cybersecurity.
Prioritize reliable existing features and real user feedback before adding more features.

## Current state

- Android Kotlin/Compose app with Firebase on the Spark plan.
- Branch: `unit_06_levels`. Latest local commit: `d3beb23`; local origin/unit_06_levels points to the same commit (remote was not freshly queried in this summary).
- Settings has separate Appearance, Learning, Security, Notifications, Privacy, and Help screens in one persistent settings window.
- Security includes password verification/change, authenticator enrollment/backup/removal, registered devices, current-device logout, and all-device app logout.
- All-device logout uses Firestore cutoff rules; it does not revoke Firebase Auth tokens. Connected apps check about once a minute; offline devices are checked on reconnect.
- Notifications provide local weekday/time reminders. The October 12 settings redesign removed the test-notification button.
- Privacy includes leaderboard visibility, JSON export, and verified account deletion with retry/recovery routing.
- Help opens an email draft addressed to `cyberityapplication@gmail.com`. Users send it from their email app.
- Developers change the support address in `app/src/main/java/com/cyberity/cvsu/SupportConfig.kt`, then rebuild/distribute.
- Firestore rules are deployed and the debug app is installed on the connected phone.
- Previous checks passed: debug build, 12 reminder tests, both Firestore rule suites, and three phone settings UI tests.
- Actual password/MFA changes, two-device logout, and actual account deletion still need dedicated test-account validation. Approved privacy policy and terms are still missing.

## Next task: verify startup, connectivity and hearts, then account flows

1. Inspect the current branch, working tree, and relevant docs before editing; preserve newer changes. First test signed-in cold launch, incomplete-profile/deletion recovery routing, airplane mode during a level, reconnection without losing the level, and changing device time without refilling hearts. Verify tab scroll retention and transitions. Then continue the account checklist below.
2. Create a concise manual acceptance checklist with pending/pass/fail results and reproducible steps.
3. Use dedicated test accounts only. Never delete or change credentials on a real student account. Do not store passwords, tokens, QR secrets, or authenticator codes in documentation.
4. Check Security navigation, cancelled verification, wrong password/code, password change and subsequent login, backup authenticator login, and factor removal.
5. With two devices or an emulator, verify all-device logout and sign-in after the cutoff. Record device/network conditions and observed timing.
6. Verify leaderboard hide/show and exported JSON contents. Test account deletion and retry/recovery only with an explicitly designated disposable account.
7. Verify Help's recipient/subject/body and cancellation without sending a report. Check reminder scheduling/cancellation.
8. Fix reproducible bugs, run checks appropriate to each fix, and document results. Mark unavailable device/account checks pending instead of claiming they passed.

If test accounts or a second device are unavailable, prepare the checklist and inspect the relevant flows while asking for the missing setup. Keep working on independent checks.

## Learning alongside the task

Explain one affected flow in plain language: what the app requests, what Firebase verifies, what the rules permit, and how failures recover. Let me practice explaining or debugging a small part myself.

## References

- `docs/security-controls.md`
- `docs/account-security-deployment.md`
- `docs/privacy.md`
- `docs/notifications.md`
- `docs/help-and-about.md`
- `docs/settings-roadmap.md`
- `firestore.rules` is the authoritative combined rules file.

## Working preferences

Proceed autonomously with routine reversible work. Keep Firebase on Spark; do not enable billing. Do not send emails or reports automatically. Do not commit or push new work unless asked. Update this handoff with completed work, evidence, and remaining tasks at the end of a session.

## Prompt to resume

Read NEXT_SESSION.md and continue the next task. Check the current repository first, prepare the account-flow acceptance checklist, and start the checks that are possible. Explain what I should learn while we work, and update the handoff when finished.

## October 12 changes and local files

Recent commits: d3beb23 (Home startup, internet cover, heart timing), a3254ed (transitions and flash fixes), 77e2dd6 (tab scroll retention), bfa3e12 (leaderboard podium/rank bar), aa86bd3 (Settings redesign and removal of placeholder/developer controls). These are commit descriptions, not newly verified device results. Earlier test results above apply to the earlier implementation.

The old feature inventory is historical: the settings redesign removed the reminder test button, in-app reset email, refresh button and duplicate logout, among other UI items. Check current code before relying on older docs.

Uncommitted files at handoff: docs/settings-roadmap.md, .firebaserc, NEXT_SESSION.md, and Python-and-Cryptography-Reviewer.pdf. Preserve the PDF; its origin and intended changes have not been reviewed. Firebase CLI project access was verified for cyberitycvsu; the Firebase plugin is installed, but dedicated plugin tools were not exposed in the prior session. Keep Spark.
