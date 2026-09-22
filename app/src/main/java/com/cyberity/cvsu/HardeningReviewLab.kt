package com.cyberity.cvsu

// ===========================================================================
// LEVEL 104 CONTENT — Security Principles, as a post-incident hardening review
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, exactly as levels 101-103.
// Story continues from 103: the tampered grade is traced to design weaknesses.

/** Clue ids reported by the hardening review through the JS bridge. */
object PrincipleClues {
    const val USER_OPENED_JDCRUZ = "user_opened_jdcruz"
    const val JOB_DESC_OPENED = "job_desc_opened"
    const val LEAST_PRIVILEGE_APPLIED = "least_privilege_applied"
    const val LOCK_LOG_OPENED = "lock_log_opened"
    const val FAILSAFE_SET = "failsafe_set"
    const val ATTACK_REPLAYED = "attack_replayed"

    /** Dangerous: switching off the one layer that actually caught the attack. */
    const val INTEGRITY_LAYER_DISABLED = "integrity_layer_disabled"
}

val principleClueLabels: Map<String, String> = mapOf(
    PrincipleClues.USER_OPENED_JDCRUZ to "Opened the student assistant's account",
    PrincipleClues.JOB_DESC_OPENED to "Read the student assistant's job description",
    PrincipleClues.LEAST_PRIVILEGE_APPLIED to "Trimmed the account to least privilege",
    PrincipleClues.LOCK_LOG_OPENED to "Read the grade-lock log",
    PrincipleClues.FAILSAFE_SET to "Set the grade lock to fail safe",
    PrincipleClues.ATTACK_REPLAYED to "Replayed the attack against the defences"
)

fun hardeningReviewLab(): LabDefinition = LabDefinition(
    levelId = 104,
    title = "Hardening Review",
    subtitle = "Security Principles · Post-incident",
    briefing = "The grade was restored, but the Registrar wants to know why the attack worked " +
            "at all — and to make sure it can't work again. Review who had access, how the " +
            "grade lock behaves when it fails, and how many defences stood in the way.\n\n" +
            "Each task explains what to look for, then hands you the console. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "hardening_review",
    startPage = "review.html",
    dangerousClues = listOf(PrincipleClues.INTEGRITY_LAYER_DISABLED),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Find the excess access",
            objective = "Open the portal accounts and compare each person's role with their " +
                    "actual job. Which account has far more access than its job needs?",
            guide = listOf(
                "A permission is a single thing an account is allowed to do. A role is a bundle " +
                        "of them, handed out as one unit because granting twenty permissions one " +
                        "at a time is tedious. That convenience is also the weakness: a role gets " +
                        "granted for one duty and quietly carries every other permission in the " +
                        "bundle along with it.",
                "Access is granted to accounts, not to people, and the two drift apart. Someone " +
                        "covers a busy week, changes duties, or finishes a contract — and the " +
                        "role stays behind. This is privilege creep, and it is almost always the " +
                        "result of a reasonable decision nobody revisited. The word " +
                        "\"temporary\" in an access note deserves more suspicion than anything " +
                        "else written there.",
                "The check itself is mechanical, and deliberately has nothing to do with trust. " +
                        "Put the job description beside the permission list and ask whether every " +
                        "permission is required by a duty written down. An account is " +
                        "over-privileged when the answer is no — regardless of whether the person " +
                        "holding it would ever misuse it, because once the password is stolen it " +
                        "is not that person deciding."
            ),
            steps = listOf(
                "Open the simulation. The review lists three areas — start with Portal accounts.",
                "Tap each of the four accounts in turn to expand it.",
                "Inside an account, expand Job description: it gives the person's duties, when " +
                        "the role was assigned, and when it was last reviewed.",
                "Below that, read the Permissions list. Pay attention to grades.edit_locked, " +
                        "records.export and users.manage — those are the wide ones.",
                "For each account, ask whether anything in the permission list is not required by " +
                        "a duty in the job description.",
                "Read the history line on every account. Three were reviewed this January; one " +
                        "was never reviewed at all.",
                "Come back and name the account whose access reaches far beyond its job."
            ),
            entryPage = "review.html",
            requiredClues = listOf(PrincipleClues.USER_OPENED_JDCRUZ),
            lockedMessage = "Open the accounts and compare each role to the person's job.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "registrar.staff — Records Officer",
                    "a.reyes — Faculty",
                    "j.dcruz — Student Assistant",
                    "guidance.office — Guidance Counselor"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Read the job description on each account, then look at the permission list.",
                "Whose job is typing in forms, but whose role can edit locked grades?"
            ),
            successFeedback = "A student assistant who encodes forms was holding a full admin " +
                    "role — granted 'temporarily' last enrollment and never removed. When that " +
                    "password was stolen, the attacker got every one of those permissions.",
            failureFeedback = "That account's permissions fit its job. Look for the one whose " +
                    "role is far wider than what the job description describes."
        ),

        LabTask(
            id = "t2",
            title = "Apply least privilege",
            objective = "Edit the over-privileged account so it keeps only what the job needs " +
                    "and save it. Then submit how many permissions you removed.",
            guide = listOf(
                "Least privilege says an account gets the smallest set of permissions that still " +
                        "lets the work happen, and nothing beyond it. Note what it does not do: " +
                        "it makes an account no harder to break into, and the password is exactly " +
                        "as stealable afterwards. What it changes is what the theft is worth.",
                "That is the whole idea — blast radius. The same stolen password against an " +
                        "encoder's account types enrollment forms. Against an admin role it " +
                        "rewrites locked grades, exports full student records and creates new " +
                        "accounts. Identical break-in, completely different incident. You can't " +
                        "make every password unstealable, so you decide ahead of time how far a " +
                        "stolen one can reach.",
                "Trim upward from the job description rather than guessing downward, and expect " +
                        "to find a floor: cut something the job genuinely needs and the work " +
                        "stops. This console refuses to save such a change, which is the polite " +
                        "version of what happens in reality — security that blocks people from " +
                        "their own work gets worked around by the people it blocks."
            ),
            steps = listOf(
                "Open the simulation, go to Portal accounts, and expand j.dcruz@cvsu.edu.ph.",
                "Re-read the Job description. It names exactly two duties, then says \"No other " +
                        "duties\".",
                "In the Permissions list, tap every permission those two duties don't require. " +
                        "The tick disappears as each one goes.",
                "Tap SAVE ACCOUNT.",
                "If the banner says it wasn't saved, you cut something the job needs — it names " +
                        "the permission, so switch that one back on and save again.",
                "If it saves with a warning, permissions beyond the job are still on. Keep " +
                        "trimming until the banner says the account has exactly what the job needs.",
                "Count how many you switched off, and come back and submit that number."
            ),
            entryPage = "roles.html",
            // Not JOB_DESC_OPENED as well: the "Job description" panel is a separate
            // collapsible sub-box, and the permissions list and SAVE ACCOUNT sit next
            // to it, not behind it — so a student can trim and save correctly without
            // ever tapping it open, and the task would stay locked despite the real
            // work being done. A correct trim already only fires when the saved set
            // matches jdcruz's two real duties exactly, which is stronger proof of
            // reading the job description than clicking an accordion is anyway.
            requiredClues = listOf(PrincipleClues.LEAST_PRIVILEGE_APPLIED),
            lockedMessage = "Read the job description, then save the account with only the " +
                    "permissions the job needs.",
            answer = LabAnswer.Text(
                accepted = listOf("5", "five"),
                placeholder = "e.g. 3"
            ),
            hints = listOf(
                "The job description lists exactly two duties.",
                "Keep the two enrollment permissions. Everything else goes."
            ),
            successFeedback = "Five permissions gone. Least privilege doesn't stop passwords " +
                    "being stolen — it shrinks what a stolen password can do. With this role, " +
                    "the attacker could only have typed enrollment forms.",
            failureFeedback = "Not the right count. Compare the permissions the account started " +
                    "with to the ones you kept."
        ),

        LabTask(
            id = "t3",
            title = "Fail safe, not open",
            objective = "The grade was locked, yet it was edited. Read the grade-lock log, set " +
                    "the lock policy to a fail-safe default, then explain why the lock didn't hold.",
            guide = listOf(
                "Every control fails eventually — the service it depends on times out, a disk " +
                        "fills, a certificate expires. What matters is which way it fails. A " +
                        "fail-safe default denies when the check can't complete; a fail-open " +
                        "default allows. That single choice decides whether a failure is an " +
                        "inconvenience or an opening.",
                "Fail-open is rarely chosen carelessly. It is chosen for availability, usually by " +
                        "someone trying to stop staff being locked out of their own tools, and " +
                        "under normal conditions it is invisible because the check almost always " +
                        "answers. It shows itself on the one night something else is broken — " +
                        "which is also when an attacker is most likely to be working.",
                "One of the options here looks like a compromise and isn't. Handing the decision " +
                        "to whoever is at the keyboard — \"this may be locked, continue?\" — is " +
                        "only a control if that person is trustworthy, and during an intrusion " +
                        "that person is the attacker. Logging has the same shape: a warning in a " +
                        "log describes the damage, it doesn't prevent it."
            ),
            steps = listOf(
                "Open the simulation and open Grade lock policy from the review areas.",
                "Expand the Grade-lock log and read its six lines in order.",
                "Note the 02:13:55 line: the lock service runs on records-db — the same server " +
                        "whose disk was filling up in the previous incident.",
                "Read the two 02:13:58 lines together. The check times out, then the policy " +
                        "decides what to do about the timeout. The edit lands four seconds later.",
                "Look at grade-lock.yaml underneath and find the on_lock_service_error setting " +
                        "that was in force that night.",
                "Pick a policy from the dropdown and tap SAVE & TEST WITH LOCK SERVICE OFFLINE. " +
                        "Try more than one — three of the four still let the edit through.",
                "Leave it on the setting that denies, then come back and say why the lock didn't " +
                        "hold."
            ),
            entryPage = "failsafe.html",
            requiredClues = listOf(PrincipleClues.LOCK_LOG_OPENED, PrincipleClues.FAILSAFE_SET),
            lockedMessage = "Read the lock log and save a fail-safe lock policy first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The attacker guessed the grade sheet's lock password",
                    "The lock check timed out, and the policy defaulted to allowing the edit",
                    "The professor unlocked the grade sheet before the attack",
                    "The integrity checker was switched off that night"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Look at what happened one second before the UPDATE.",
                "When the lock service can't answer, what does on_error tell the portal to do?"
            ),
            successFeedback = "The lock service sat on the database server whose disk was " +
                    "filling up. It stopped answering, and the portal treated 'no answer' as " +
                    "'yes'. A fail-safe default denies when a check can't complete.",
            failureFeedback = "The log shows what actually happened. Read the two lines right " +
                    "before the UPDATE."
        ),

        LabTask(
            id = "t4",
            title = "Stack the layers",
            objective = "Switch on the defences that were missing, then replay the 02:14 " +
                    "attack. When every layer holds, the replay report shows a flag — submit it.",
            guide = listOf(
                "Defence in depth means several independent controls standing between an " +
                        "attacker and what they are after, each able to stop or catch the attack " +
                        "on its own. The point isn't that any one layer is excellent. It's that " +
                        "none of them has to be, because the attack has to get past all of them.",
                "Independence is what makes a layer count. Four password rules are one layer " +
                        "wearing four hats — a stolen password defeats the lot at once. A network " +
                        "restriction, a second authentication factor, an alert and a signed hash " +
                        "fail for entirely unrelated reasons, so no single mistake can switch them " +
                        "all off together.",
                "Detection earns a place in the stack even though it prevents nothing. A control " +
                        "that pages the Registrar at 02:14 doesn't stop the edit, but it turns a " +
                        "breach found two days later by a professor into one handled while the " +
                        "session is still open. And depth is only ever built by adding: taking " +
                        "away a control that already works has never made a stack deeper."
            ),
            steps = listOf(
                "Open the simulation and open Defence layers.",
                "Before changing anything, tap REPLAY THE 02:14 ATTACK and read the report — it " +
                        "shows what each layer did with the settings exactly as they are.",
                "Notice which single layer is already on, and what it caught during the real " +
                        "incident.",
                "Tap each switched-off layer to switch it on.",
                "Leave Layer 4 · Integrity alone. Switching off the one control that caught the " +
                        "attack is a mistake the console will call out, and it costs you a heart.",
                "Replay the attack again. The report only prints its flag once all four layers " +
                        "hold.",
                "Come back and submit the flag in full, CYBERITY{...} wrapper included."
            ),
            entryPage = "layers.html",
            requiredClues = listOf(PrincipleClues.ATTACK_REPLAYED),
            lockedMessage = "Replay the attack at least once.",
            answer = LabAnswer.Flag(
                sha256 = "6fa00f34533e0f21140213d67d0d2393d432b7e161cb689345c1f99ef9551149"
            ),
            hints = listOf(
                "Replay with the current settings first and see which layers let it through.",
                "Every layer should be on. The one that was already on should stay on."
            ),
            successFeedback = "Four independent layers, each able to stop or catch the attack " +
                    "on its own. That's defence in depth: no single control has to be perfect.",
            failureFeedback = "Not the flag. It only appears in the replay report when every " +
                    "layer is switched on."
        ),

        LabTask(
            id = "t5",
            title = "Write the fix list",
            objective = "Match each fix you made to the security principle behind it.",
            guide = listOf(
                "Three fixes, three different principles. Naming them is what makes the work " +
                        "portable — the specific fixes only ever apply to this portal, but the " +
                        "principles behind them apply to any system you are asked to harden.",
                "Each one answers a different question. Least privilege asks how much access a " +
                        "single account should carry. Defence in depth asks how many independent " +
                        "things have to fail before an attack succeeds. Fail-safe defaults ask " +
                        "what a control does at the moment it can no longer do its job.",
                "None of the three stops a password being stolen, and that is worth sitting with. " +
                        "The theft was always going to happen; the Registrar's real question was " +
                        "why it was worth so much. Together these answer it — the password now " +
                        "reaches almost nothing, several unrelated controls stand in its way, and " +
                        "the one that breaks refuses instead of waving the edit through."
            ),
            steps = listOf(
                "Open the simulation and use ← Review to get back to the front page.",
                "Look over the three review areas and recall the change you made in each: the " +
                        "trimmed role, the lock policy, the layer stack.",
                "For each change, ask which question it answered — how much access, how many " +
                        "barriers, or what happens on failure.",
                "Come back and match all three at once."
            ),
            entryPage = "review.html",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Trim role: defence in depth · Add layers: fail-safe · Lock policy: least privilege",
                    "Trim role: least privilege · Add layers: defence in depth · Lock policy: fail-safe defaults",
                    "Trim role: fail-safe defaults · Add layers: least privilege · Lock policy: defence in depth",
                    "Trim role: least privilege · Add layers: fail-safe defaults · Lock policy: defence in depth"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Which fix reduced what one account can do? Which fix added more barriers? " +
                        "Which fix changed what happens when something breaks?"
            ),
            successFeedback = "Least privilege limits the damage, defence in depth means one " +
                    "failure isn't fatal, and fail-safe defaults make failures deny instead of " +
                    "allow. Together they turn a single stolen password into a dead end.",
            failureFeedback = "At least one fix is matched to the wrong principle. Think about " +
                    "what each fix changed: how much access, how many barriers, or what happens on failure."
        )
    )
)
