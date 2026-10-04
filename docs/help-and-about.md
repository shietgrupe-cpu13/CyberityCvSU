# Help & About

`HelpSettingsScreen()` provides content inside the existing Settings window. It does not create another window or change app navigation.

- The app version comes from Android package metadata; lookup failure displays `Unknown`.
- Expandable help topics explain learning, appearance/tutorial preferences, password reset, authenticators/recovery, Firestore device sessions and local reminders.
- Problem reports have a category and up to 4,000 characters of user-written detail. Their preview shows the exact text used by Copy and Email support.
- Diagnostics are off by default. Opting in adds only app version, manufacturer/model and Android release/API level. No Firebase user, student ID, email, token, password or app log is fetched.
- Copy writes the report to Android's clipboard. Email support opens an email app with the recipient, subject and report body filled in. The user reviews and presses Send in that app; Cyberity does not send automatically. The current recipient is cyberityapplication@gmail.com, matching the Firebase CLI account.
- The screen warns users against entering private information themselves. User-entered details are not automatically redacted. Draft state survives configuration changes via Compose saveable state; it is not uploaded by this screen.
- Terms are explicitly described as unconfigured; no legal terms are fabricated.

Device verification: expand/collapse a help topic; check version against App info; enter a report; compare preview and clipboard; toggle diagnostics; open and cancel the email chooser; confirm nothing says the report was submitted; check keyboard scrolling, large text, and both themes. Confirm the email draft recipient, subject and body before sending.

## Changing the support inbox

Edit the EMAIL constant in app/src/main/java/com/cyberity/cvsu/SupportConfig.kt, then rebuild and distribute the app. This one value controls the recipient, displayed address and fallback instructions. Changing the Firebase login email does not automatically change the support inbox in installed apps.
