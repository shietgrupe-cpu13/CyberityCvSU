# Settings roadmap

Settings opens a full-screen dashboard with separate Appearance, Learning,
Security, Notifications, Privacy, and Help & About destinations. Back returns to
the dashboard and Close exits settings. Privacy content has its own screen.

Working settings: theme, lesson/lab font size, tutorial replay, learning reminders
with selected weekdays/time, notification permission controls and a test action, authenticator
enrollment, backup authenticator enrollment, authenticator removal, verified
password change, password-reset email and local logout. Tracked devices/global logout
use Firestore on Spark. The merged rules are deployed; two-device app verification
is still required as described in account-security-deployment.md.

The remaining suggestions below are roadmap work, not active switches/actions.
Unsupported Security roadmap items are omitted from the finished controls.
Learning notification preferences now schedule real local reminders.

Privacy now offers leaderboard visibility, JSON account export, and verified
account deletion with retry/recovery routing. Help & About provides FAQs and
user-controlled copy/share problem reports with optional basic diagnostics.
Reports have no preconfigured support recipient. Firestore Privacy rules were
deployed and the integrated debug app installed on 2026-10-04. See privacy.md.

| Section | Suggested feature | Work required |
| --- | --- | --- |
| Appearance | Reduced motion | Persist a preference and apply it consistently to animations. |
| Learning | Offline lessons | Define supported content, downloads, cache storage and progress synchronization. |
| Notifications | Security alerts | Server-owned security events, FCM delivery and safe notification content. |
| Security | Sign out selected device | Server-bound session identity with per-session enforcement across protected resources. |
| Security | Approximate location | Server-derived IP geolocation, privacy disclosure and accuracy caveats. |
| Security | Recent activity | Server-owned audit events, timestamps, retention and account-scoped access. |
| Privacy | Privacy policy | An approved policy matching actual collection, retention and support practices. |
| Help | Terms of use | Approved terms and a maintained publication location. |

Suggested order: verify the account controls on test accounts, then trusted
account recovery, security activity/alerts, privacy controls and data lifecycle,
then offline learning. See notifications.md for the reminder implementation and
device verification steps.
