# Registration and sign-in validation

Registration requires a unique username, first/last name (middle name optional), student ID, year level, CvSU email, a password satisfying the configured policy, and matching password confirmation.

The selected policy is at least 8 characters, with A–Z, a–z, 0–9, and a Firebase-supported non-alphanumeric character. Guidance and missing-requirement feedback appear below the password field. The live Firebase project policy was read and already matches these requirements, with enforcement ENFORCE and forceUpgradeOnSignin false; no server settings needed changing. Existing passwords are not rejected by local registration validation during login.

A username collision is verified during the atomic profile/claim transaction after account submission. It appears directly beneath the username field, not as an unverified typing-time availability result. Changing the username clears the error; final claim ownership is always checked again on submission. Firebase sign-in is required to read claims, so the app does not expose unauthenticated claim queries.

Wrong passwords and unknown email accounts share “Invalid email or password. Please try again.” Network failures and throttling show distinct connection/wait messages. Generic registration errors replace raw backend details. Unit tests exercise each password requirement and equivalent invalid-login messages. No production student account was created for testing.
