# Authentication safeguards

The app continues to use Firebase Authentication directly. These changes add
device-local request throttling and safe feedback; they do not implement a
server-side account lockout or prevent calls made directly to Firebase's API.
Clearing app data, modifying the client, or using another device can bypass a
local throttle. Do not treat it as the authoritative anti-brute-force control.

## App behavior

- Password and TOTP sign-in share a device-wide failure counter. Switching
  emails, leaving the screen, or restarting the app does not reset it.
- Five rejected credentials trigger a 30-second cooldown. Further groups of
  five increase it to 60, 120, 240, 480, then at most 900 seconds.
- Full authentication resets the history. An MFA challenge and network errors
  do not count as incorrect credentials or erase previous failures. History
  also expires after 15 minutes without a rejected credential.
- Only one request per operation can be in flight, with at least one second
  between sign-in requests. Password reset requests have a separate 60-second
  interval. Firebase throttling responses impose at least a 60-second local
  pause; the server may continue to reject requests for longer.
- Sign-in errors do not distinguish nonexistent, disabled, or incorrect-password
  accounts. Password reset feedback does not disclose missing accounts.
- SharedPreferences store counters and timestamps only, never credentials,
  email addresses, codes, or tokens. The throttle file is excluded from backup
  and device transfer because its state belongs to the original device.
- A slow student-profile check displays connection feedback after eight seconds
  and a retry screen at 30 seconds. Successful validation always proceeds
  immediately. Leaving the screen or timing out cancels acceptance of old
  results; a retry creates a new validation attempt. No profile check is bypassed.

## Firebase configuration to verify before release

No Firebase console or backend configuration was changed by this implementation.

1. Verify email enumeration protection is enabled in the project's Authentication
   settings. Generic app messages alone cannot protect responses from the API.
2. Review Firebase Authentication quotas, usage, and suspicious traffic. Firebase's
   published limits are not a configurable five-attempt account-lockout policy.
3. Evaluate App Check for Authentication with Play Integrity for this project.
   Authentication support is documented as Preview. Configure providers and test
   legitimate builds before enforcing it; an unconfigured client can be blocked.
4. Preserve existing TOTP MFA and verify it with a test account. Never store or
   check passwords or MFA secrets in a Firestore collection to implement lockouts.
5. If a mandatory account/IP rate-limit policy is needed beyond Firebase's managed
   protections, design it in a trusted authentication service. A client-written
   Firestore attempt counter cannot enforce a policy on direct Firebase sign-ins.

References:

- [Firebase password authentication and email enumeration protection](https://firebase.google.com/docs/auth/android/password-auth)
- [Firebase Authentication limits](https://firebase.google.com/docs/auth/limits)
- [Firebase App Check](https://firebase.google.com/docs/app-check)
- [Firebase TOTP MFA](https://firebase.google.com/docs/auth/android/totp-mfa)

## Device verification

Use a dedicated test account; do not intentionally throttle a real student's account.
Check five incorrect passwords, cooldown countdown and expiry, restart during a
cooldown, changing the email during a cooldown, five incorrect TOTP codes, a correct
MFA sign-in, airplane-mode errors, and reset-email cooldown. Confirm full sign-in
resets local history and bad credentials produce the same generic message.

For startup, verify cold launch/restart in Light, Dark, and System modes. Test a
slow profile read, timeout/retry, sign-out during a pending read, and delayed results
from an earlier attempt. Confirm validation and tutorial/Level 0 routing are preserved.
