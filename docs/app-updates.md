# Manual update checks

Settings > Help & About > Check for updates fetches the latest published release from the existing public GitHub repository. Versions are compared numerically. A newer version offers Download and Cancel. Download opens the exact CyberityCvSU.apk release asset in the browser; the user completes installation through Android. Cancel and dialog dismissal have no persistent suppression, so checks can be repeated. Equal or older releases show “You’re up to date.” Network/API failures show a retry message rather than reporting that the app is current. No GitHub token or account identity is sent. Draft/prerelease results and untrusted asset URLs are rejected.

The existing download APK uses the Android debug key; future builds must preserve signing identity for installation over the current app. Android unit tests cover numeric version ordering and malformed versions. Real-device browser and installation testing remains separate.
