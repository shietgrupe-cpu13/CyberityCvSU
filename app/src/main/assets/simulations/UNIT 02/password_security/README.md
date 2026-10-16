# Unit 02 simulation structure

Each playable level has a Kotlin content file and its own asset directory, following the level layout used in Units 01 and 03.

| Level | Kotlin content file | Simulation directory |
| --- | --- | --- |
| 201 | StrongPasswordsLab.kt | strong_passwords |
| 202 | PasswordAttacksLab.kt | password_attacks |
| 203 | MultiFactorAuthenticationLab.kt | multi_factor_authentication |
| 204 | AccountProtectionLab.kt | account_protection |
| 205 | PasswordSecurityChallengeLab.kt | password_security_challenge |
| 250 | HashCrackingPracticeLab.kt | hash_cracking_practice |

The directories are siblings under `app/src/main/assets/simulations/UNIT 02/`. Each contains three task HTML pages and a `styles.css` that imports this directory's shared stylesheet.

This `password_security` directory holds the reusable account workspace, hash engine, case data, lesson data, script, and styles. Edit its `styles.css` to change typography across all six Unit 02 simulations. Lesson text supports `**bold**` emphasis. App font-size preferences are applied by `LabScreen` through WebView text zoom.

`LearnScreen.contentFor` routes each level to its own lab function. `PasswordSecurityLab.kt` builds the shared task workflow. Unit 02's walkthrough (level 200) remains in `UnitTwoWalkthroughScreen.kt`.

`people.js` supplies the scripted fictional conversations with Maya (student account), Jules (campus IT), and Elena (registrar). Each task has its own request and follow-up answer; the account controls and login records provide the practical investigation.

The first password lesson focuses on generated, unique credentials. Hash comparisons are taught in the attack and audit lessons. Verified repairs or correct hash matches issue one CASE code directly and log the outcome in the continuing case journal. Submission stays locked until the simulation reports that task's evidence through the Android bridge.
