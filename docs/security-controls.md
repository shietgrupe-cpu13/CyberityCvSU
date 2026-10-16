# Completed Security controls on Spark

Security now has account/verification status, password change and reset email,
authenticator setup, adding a backup authenticator, removing an enrolled TOTP
authenticator, device-list refresh, local logout, and all-device app logout.
Unsupported roadmap cards were removed from the active screen; selective remote
logout, location, and a trusted security audit history are still out of scope.

Password change, enrollment/removal and all-device logout use the shared
AccountVerificationDialog. It verifies the current password, resolves enrolled
MFA, verifies the account UID did not change, and refreshes the token before the
action. The existing request throttle covers verification attempts. Cancelling
verification does not run an action. Sensitive password, code and QR dialogs
use FLAG_SECURE, and credentials/secrets are never saved in preferences.

Password change requires matching nonblank values of at least 12 characters;
Firebase also enforces the project's own policy. The app clears entered new
password values when submitting and signs out after confirmed success.

Adding a backup enrolls another independent TOTP factor. Sign-in and
reauthentication let users choose among enrolled TOTP factors. Moving to a new
authenticator means adding/verifying it first, then removing the old factor.
Removing the last authenticator explicitly warns that it disables this
protection. Firebase can invalidate a session after unenrollment; the app handles
that by directing the user to sign in and check the resulting factor status.

Recovery guidance is truthful: Cyberity does not issue Firebase recovery codes,
and resetting a password does not bypass MFA. Keep a working backup authenticator
or a securely backed-up authenticator account. Administrative recovery requires
identity verification and is not implemented as a client-side bypass.

All-device logout uses the existing Spark cutoff/rules. It remains available
even if no devices are listed or the list fails to load. Device metadata and
factor status are refreshed separately, with retry feedback on slow/failed loads.

Validation: debug build passed and the updated app was installed on the connected
phone. Both focused UI tests passed, covering window persistence, absence of
roadmap controls, and cancellation of sensitive-action verification.
Live password and MFA mutations must be tested using a dedicated account:
wrong password/code, backup sign-in, remove old factor, remove last factor,
password-policy rejection, successful password change and subsequent sign-in,
network errors, and two-device global logout. Do not perform these destructive
credential changes on a real student account during automated UI validation.

Reference: https://firebase.google.com/docs/auth/android/totp-mfa
