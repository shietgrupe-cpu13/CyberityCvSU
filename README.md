# Cyberity

Cyberity is an Android cybersecurity e-learning capstone application built with Kotlin, Jetpack Compose, and Firebase. Students learn through guided lessons, quizzes, and interactive simulations while tracking their progress and daily learning streaks across six learning units.

## Download

[Download CyberityCvSU.apk — v1.0](https://github.com/shietgrupe-cpu13/CyberityCvSU/releases/download/v1.0/CyberityCvSU.apk) · [Release notes](https://github.com/shietgrupe-cpu13/CyberityCvSU/releases/tag/v1.0)

**Version 1.0 — Stable release.** Requires Android 7.0 or later. The APK is signed with the current Android debug key. Android may ask you to allow installation from your browser or file manager.

## Features

- Six units covering cybersecurity fundamentals, account security, phishing and social engineering, malware, network security, and incident response.
- Interactive simulations and quizzes with feedback, hearts, EXP, and purchasable hints.
- A ranking system and leaderboard for comparing learning progress.
- A flame icon and daily learning streak saved in Firestore. Complete a level or quiz to light it; server time prevents phone date/time changes from granting extra days. Database administrators can correct or reset streaks.
- Student profiles with a username, student ID, and year level. Existing students can add their year level through **Settings > Student information**.
- PDF completion certificates populated with the student's username, student ID, and year level, with spaces for three project members' signatures.
- Completion and EXP awards saved together to prevent duplicate first-clear rewards. Hint purchases also save their receipt and EXP deduction together.
- Pending completion recovery when a save needs to be retried.

## Certificates and tester mode

Open **Settings > Completion certificate** to export a PDF after completing the required levels and adding the required student information. Optional walkthrough and reward nodes do not count as required levels. Signature names and roles can be edited in `CertificateBranding` in [CertificatePolicy.kt](app/src/main/java/com/cyberity/cvsu/CertificatePolicy.kt).

For development, an administrator can set the Boolean field `isTester` to `true` on the student's `users/{uid}` document in the Firebase console. The app reads the server-confirmed flag to enable level access and a watermarked certificate preview. Set it to `false` to revoke access. Students cannot change this field themselves. Preview certificates are clearly marked and do not certify course completion.

See [tester mode](docs/developer-mode.md), [student information and certificates](docs/student-information-and-certificates.md), and [reliable progress and EXP saves](docs/atomic-learning-saves.md) for implementation details.

See [learning streaks](docs/learning-streak.md) for daily reset behavior, clock protection, and administrator controls in Firebase Console.

## Build and run

1. Open this repository in Android Studio.
2. Install Android SDK 37. The Gradle daemon configuration uses JDK 25; Android source compatibility is Java 17.
3. Configure Firebase Authentication and Cloud Firestore for your project, and provide its Android `google-services.json` in `app/`. Never commit service account keys or private signing keys.
4. Build and run the `app` configuration, or use the Gradle wrapper:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

The generated APK is `app/build/outputs/apk/debug/app-debug.apk`. Download the published `CyberityCvSU.apk` and its SHA-256 checksum from [GitHub Releases](https://github.com/shietgrupe-cpu13/CyberityCvSU/releases). Rebuild and upload a new release asset when preparing the next distribution.

Firestore access rules are in [firestore.rules](firestore.rules), with emulator tests in [security-tests](security-tests). Deploy rules to the intended Firebase project before testing against a new backend. Firebase console tester changes require administrator access.

## Project layout

| Location | Contents |
| --- | --- |
| `app/src/main/java/com/cyberity/cvsu/` | Android screens, learning flow, repositories, and certificate generation |
| `app/src/main/assets/` | Bundled simulation and learning assets |
| `app/src/test/` | Local unit tests |
| `security-tests/` | Firestore rules tests and security utilities |
| `docs/` | Feature behavior and development notes |
