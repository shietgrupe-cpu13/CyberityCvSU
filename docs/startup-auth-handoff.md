# Startup and authentication handoff

Saved October 3, 2026. User plans to resume October 4 and will initiate the work.
Do not automatically resume, deploy Firebase changes, or commit/push these changes.

## Completed, currently uncommitted

- Replaced the separate, two-second splash Activity with AndroidX SplashScreen
  on MainActivity and a 180 ms exit fade. Reused CYBERITY logos and existing colors.
- Preserved Firebase authentication, profile/student-ID checks, and tutorial flow.
- Added profile loading feedback after eight seconds and timeout/retry after
  30 seconds. Ready results proceed immediately; old callbacks are ignored after
  cancellation or timeout.
- Added device-local password/MFA throttling: five failures trigger 30 seconds,
  escalating to 15 minutes; persistence across restarts; duplicate-request guard.
- Added a separate one-minute password-reset request limit and generic auth errors.
- Added tests and documented limitations in authentication-safeguards.md.

## Next work agreed for discussion/resumption

Prioritize verifying Firebase-side protection before tightening local cooldowns.
The app's local throttle can be bypassed by direct Firebase API calls, another
device, clearing app data, or a modified client. It is an additional safeguard,
not an authoritative server-side anti-brute-force policy.

1. Verify Firebase email enumeration protection is enabled.
2. Assess Identity Platform reCAPTCHA bot protection for email/password auth;
   begin with audit mode before enforcement, checking project eligibility,
   SDK compatibility, costs, and legitimate sign-in behavior.
3. Evaluate App Check with Play Integrity. Authentication support is Preview;
   confirm provider setup and legitimate builds before enabling enforcement.
4. Preserve existing TOTP MFA. Consider separate password and MFA failure counters
   with a shared request limit, so password mistakes do not consume MFA retries.
5. Keep temporary, progressive cooldowns and recovery access. Avoid permanent
   account lockouts that can be abused to deny students access.
6. No console/backend configuration has been inspected or changed. Determine
   available authorized access before making external configuration changes.

## Validation and remaining checks

Passed: assembleDebug, testDebugUnitTest (17 tests), lintDebug (0 errors, 43
existing warnings, 1 hint), and assembleDebugAndroidTest.

Updated debug app installed on the connected Android API 36 device without
clearing app data. Existing signed-in session reached Learn successfully.
Android reported cold launch 1045 ms and repeat/warm launch 82 ms.

UI test APK installation was rejected with INSTALL_FAILED_USER_RESTRICTED,
"Install canceled by user". Do not retry that installation without renewed
authorization. UI tests compiled but did not execute. The emulator stayed offline.

Remaining manual checks: live password/MFA failures using a dedicated test
account, logged-out startup, slow/offline Firebase reads, timeout/retry and late
callbacks, tutorial/Level 0 routing, Light/Dark/System transitions, logo cropping,
and frame-by-frame checks for flashing or duplicate splashes. Do not clear the
user's app data or trigger lockouts on real student accounts for testing.

## References

- [Authentication safeguards and release notes](authentication-safeguards.md)
- [Firebase password authentication](https://firebase.google.com/docs/auth/android/password-auth)
- [Identity Platform reCAPTCHA integration](https://docs.cloud.google.com/identity-platform/docs/recaptcha-enterprise)
- [Firebase App Check](https://firebase.google.com/docs/app-check)
- [OWASP authentication guidance](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
