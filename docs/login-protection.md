# Login protection review — 2026-10-12

## Implemented

AuthAttemptGuard now uses Android elapsedRealtime, including time spent asleep,
instead of the user-editable wall clock. The same guard covers password login,
MFA completion and sensitive account verification. Password-reset requests keep
a separate minimum one-minute interval.

Five credential failures trigger 30 seconds of cooldown. Further groups escalate
to 60, 120, 240, 480 and at most 900 seconds. An MFA challenge or network error
does not reset failure history. Only completed authentication resets the budget.

Failure counts, penalty, deadlines, elapsed-time checkpoint and Android boot count
are synchronously persisted. Within the same boot, process restarts retain the
original deadlines. Across a reboot or unavailable boot identity, saved remaining
waits are reapplied conservatively; rebooting cannot immediately clear a penalty.
Legacy wall-clock penalties migrate conservatively to at most 15 minutes. This
can briefly retain a penalty that would otherwise have expired, without trusting
the editable date/time. No credentials, email addresses or tokens are saved.

This is client-side protection. Clearing app data, modifying the client or calling
Firebase directly bypasses it. Session documents and Firestore counters do not
enforce Firebase Authentication request limits.

## Validation

Debug build passed. All 12 AuthThrottleTest unit tests passed, including elapsed
time restoration, reboot/rollback restoration, password-to-MFA shared failure
budget, escalation, duplicate requests, network errors and server-throttle waits.
Real-device process termination, reboot and date-change checks remain manual;
unit tests cover the state logic and are not a live attack test.

## Live read-only project review

Project cyberitycvsu, using existing Firebase CLI login:

- Email/password provider enabled.
- Improved email privacy / email enumeration protection enabled.
- Password policy ENFORCE: minimum 8 characters; uppercase, lowercase, numeric
  and non-alphanumeric characters required. App password-change UI also requires
  at least 12 characters; project policy was not changed.
- MFA enabled.
- No reCAPTCHA configuration returned by the admin config endpoint.
- App Check UNENFORCED for Authentication and Firestore.

No cloud settings, providers, billing, users or passwords were changed.
`security-tests/review-auth-config.cjs` reproduces the read-only review using the
installed Firebase CLI package and prints only selected configuration fields.

## Recommended next server protection

Integrate Android App Check / Play Integrity, register the app's signing identity,
verify debug and distributed builds, and observe valid/invalid request metrics
before enabling Authentication enforcement. Existing installed clients need
compatible App Check integration before enforcement; flipping the server setting
immediately can block legitimate sign-in. App Check reduces requests from
unverified clients; it does not guarantee a per-account five-attempt lockout.

reCAPTCHA Enterprise bot protection involves billable components; do not enable
it while the project must remain on Spark without a billing review.

Sources:
- https://firebase.google.com/docs/auth/limits
- https://docs.cloud.google.com/identity-platform/docs/admin/app-check-integration
- https://docs.cloud.google.com/identity-platform/docs/admin/email-enumeration-protection
- https://docs.cloud.google.com/identity-platform/docs/recaptcha-enterprise
