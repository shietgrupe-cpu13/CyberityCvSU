package com.cyberity.cvsu

// ===========================================================================
// LEVEL 603 CONTENT — Containment, stopping the bleeding on Monday morning
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 601-602:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 06/containment_ops.
//
// Same case as 601-602, now live. Four staff accounts are in the attacker's
// hands and one is still sending phishing. The level is about doing the
// smallest thing that actually stops the harm, keeping the evidence while you
// do it, following the harm to new victims, and proving the attacker is out.
// Cleaning up and restoring is 604.

/** Clue ids reported by the containment console through the JS bridge. */
object ContainmentClues {
    const val CONTAINED_RAMOS = "contained_ramos"
    const val EVIDENCE_SAVED = "evidence_saved"
    const val RULE_REMOVED = "rule_removed"
    const val ALL_CONTAINED = "all_contained"
    const val BLOCKED_IP = "blocked_ip"
    const val BLOCKED_DOMAIN = "blocked_domain"
    const val PURGED = "purged"
    const val CLICKS_SEEN = "clicks_seen"
    const val STUDENTS_CONTAINED = "students_contained"
    const val MFA_REMOVED = "mfa_removed"
    const val VERIFY_PASSED = "verify_passed"

    /** Dangerous: actions that are out of proportion or destroy evidence. */
    const val SHUTDOWN_EMAIL = "shutdown_email"
    const val WIPED_PC = "wiped_pc"
    const val DELETED_UNSAVED = "deleted_unsaved"
    const val GEO_BLOCK = "geo_block"
}

val containmentClueLabels: Map<String, String> = mapOf(
    ContainmentClues.CONTAINED_RAMOS to "Stopped the sending from Prof. Ramos's account",
    ContainmentClues.EVIDENCE_SAVED to "Saved her evidence to the locker",
    ContainmentClues.RULE_REMOVED to "Removed her forwarding rule",
    ContainmentClues.ALL_CONTAINED to "Contained all four staff accounts",
    ContainmentClues.BLOCKED_IP to "Blocked the attacker's IP at the firewall",
    ContainmentClues.BLOCKED_DOMAIN to "Blocked the phishing domain at campus DNS",
    ContainmentClues.PURGED to "Purged the phishing emails from student inboxes",
    ContainmentClues.CLICKS_SEEN to "Checked who clicked the link",
    ContainmentClues.STUDENTS_CONTAINED to "Contained the two student accounts",
    ContainmentClues.MFA_REMOVED to "Removed the attacker's backdoor device",
    ContainmentClues.VERIFY_PASSED to "Passed the re-entry test"
)

fun containmentLab(): LabDefinition = LabDefinition(
    levelId = 603,
    title = "Stop the Bleeding",
    subtitle = "Containment · ITSO",
    briefing = "Monday, 8:20 AM. The incident team has accepted your report and the lead's " +
            "answer is short: \"You found it, you contain it.\"\n\n" +
            "Four staff accounts are in the attacker's hands, and Prof. Ramos's account is " +
            "still sending phishing to students. A counter on the console shows every email " +
            "that goes out while you work.\n\n" +
            "Containment means stopping the harm from spreading, now, with the smallest action " +
            "that actually works. It is not cleaning up and it is not punishing anyone. Keep " +
            "the evidence as you go. Actions that are out of proportion or destroy evidence " +
            "cost a heart.\n\n" +
            "Each task explains what to look for, then hands you the console. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 06/containment_ops",
    startPage = "ops.html",
    dangerousClues = listOf(
        ContainmentClues.SHUTDOWN_EMAIL,
        ContainmentClues.WIPED_PC,
        ContainmentClues.DELETED_UNSAVED,
        ContainmentClues.GEO_BLOCK
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Stop the sending",
            objective = "Prof. Ramos's account is still sending phishing. **Read her active " +
                    "sessions**, then use the action panel to **stop it**. Then answer **why a " +
                    "password reset alone doesn't work**.",
            guide = listOf(
                "**Containment** is the step between finding an incident and fixing it: **stop " +
                        "the damage from spreading** so the problem stops growing. The first " +
                        "question is always **what is causing harm right now**, and the answer " +
                        "here is a counter that keeps going up.",
                "When you sign in, you get a **session**: a token that keeps you signed in " +
                        "without typing your password again. **Changing the password doesn't " +
                        "end sessions that already exist.** An attacker whose mail app is " +
                        "already signed in keeps working until those sessions are **revoked**.",
                "Choose the **smallest action that fully works**. Shutting email down for the " +
                        "whole campus stops the attacker, but also stops 20,000 people who did " +
                        "nothing wrong. Wiping a PC hits the **wrong target** (the attacker is " +
                        "in her account, not her computer) and destroys evidence."
            ),
            steps = listOf(
                "Open the console. It starts on **l.ramos**, with the **phishing sent** counter " +
                        "running at the top.",
                "Read **ACTIVE SESSIONS**. Find the one that isn't hers.",
                "Use the **CONTAIN** actions. Watch the **phishing sent** counter: did it stop?",
                "**Don't** shut down campus email or wipe her PC. Each costs a heart.",
                "**Swipe the bar** down and answer why a password reset alone fails."
            ),
            entryPage = "account.html#ramos",
            requiredClues = listOf(ContainmentClues.CONTAINED_RAMOS),
            lockedMessage = "Stop the sending from Prof. Ramos's account first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "A password reset takes 24 hours to apply",
                    "The attacker guessed the new password straight away",
                    "The attacker's mail app was already signed in, and a password change " +
                            "doesn't end sessions that already exist",
                    "The emails had all been scheduled in advance"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Look at the session from 203.0.113.47. When was it created, and how long is " +
                        "it valid?",
                "Only one action in the panel ends that session."
            ),
            successFeedback = "Sending stopped. The attacker's mail app had been signed in since " +
                    "2:10 AM Saturday on a token valid for 90 days. A new password does nothing " +
                    "to a session that already exists. Revoking every session, blocking sign-in " +
                    "and then resetting the password cut it off in one move, without touching " +
                    "anyone else's email.",
            failureFeedback = "Not that. Look at the active sessions: the attacker never needed " +
                    "her password again after Saturday."
        ),

        LabTask(
            id = "t2",
            title = "Snapshot, then scrub",
            objective = "Her hidden forwarding rule is still copying her mail to the attacker. " +
                    "**Save the evidence first**, then **remove the rule**. Submit the " +
                    "**address** it was forwarding to.",
            guide = listOf(
                "Containment changes things, and changing things can **erase evidence**. The " +
                        "rule, the sign-in history and the mailbox audit log show **what the " +
                        "attacker did, when and from where**. Investigators, the DPO and maybe " +
                        "the police will need them later.",
                "So the habit is **snapshot, then scrub**: copy the evidence to a safe place " +
                        "(an **evidence locker**), note **who saved it and when**, and only " +
                        "then remove the attacker's changes.",
                "Removing the rule is still urgent: every email Prof. Ramos receives is being " +
                        "copied to the attacker until it's gone. Saving takes seconds. Don't " +
                        "skip it."
            ),
            steps = listOf(
                "On **l.ramos**, open **MAILBOX RULES** and read the rule she didn't make.",
                "Tap **SAVE EVIDENCE TO LOCKER** before anything else.",
                "Then tap **DELETE RULE**. Deleting before saving costs a heart.",
                "**Swipe the bar** down and type the **address** her mail was forwarded to."
            ),
            entryPage = "account.html#ramos",
            requiredClues = listOf(
                ContainmentClues.EVIDENCE_SAVED,
                ContainmentClues.RULE_REMOVED
            ),
            lockedMessage = "Save her evidence to the locker, then remove the rule.",
            answer = LabAnswer.Text(
                accepted = listOf("r4mos.l@mailbox.example"),
                placeholder = "name@domain"
            ),
            hints = listOf(
                "It's in the rule itself, and in the evidence locker entry.",
                "It looks like her name, but it isn't a CvSU address."
            ),
            successFeedback = "r4mos.l@mailbox.example, a look-alike of her own name on an outside " +
                    "mail service. That address is now in the locker with the rule's creation " +
                    "time and the IP that made it, saved before the rule was deleted. The " +
                    "attacker stops receiving her mail, and the proof survives.",
            failureFeedback = "Not that address. Open the rule or the locker entry and copy the " +
                    "address after the arrow."
        ),

        LabTask(
            id = "t3",
            title = "Block the indicators",
            objective = "Contain the **other three accounts**, then block the attacker's **IP " +
                    "at the firewall** and the phishing **domain at campus DNS**. Then answer " +
                    "**what the domain block protects**.",
            guide = listOf(
                "Contain **every** affected account, not just the one that was reported. " +
                        "Detection (602) found three more. Contain them all **at the same " +
                        "time**, or the attacker sees one door close and hurries through the " +
                        "others.",
                "Then block the **indicators of compromise** across the network. A **firewall " +
                        "block** stops the attacker's IP from reaching campus systems. A **DNS " +
                        "block** stops campus devices from finding the phishing site's address " +
                        "at all, so a link clicked on campus Wi-Fi goes nowhere.",
                "Block precisely. Blocking **one IP and one domain** is proportionate. Blocking " +
                        "**a whole country** cuts off exchange students and staff travelling " +
                        "abroad, and the attacker can simply switch to another IP anyway."
            ),
            steps = listOf(
                "Back on the console, open **r.delacruz**, **a.mendoza** and **j.bautista** in " +
                        "turn. For each, **save evidence**, **contain**, and **delete the rule**.",
                "Tap **NETWORK BLOCKS**.",
                "Block **203.0.113.47** at the firewall and **cvsu-payroll.example** at DNS.",
                "**Don't** block a whole country. It costs a heart.",
                "**Swipe the bar** down and answer what the DNS block protects."
            ),
            entryPage = "ops.html",
            requiredClues = listOf(
                ContainmentClues.ALL_CONTAINED,
                ContainmentClues.BLOCKED_IP,
                ContainmentClues.BLOCKED_DOMAIN
            ),
            lockedMessage = "Contain all four staff accounts and add both network blocks first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "It deletes the phishing emails from every inbox",
                    "It signs the attacker out of the four accounts",
                    "Anyone on the campus network who clicks the link from now on, including " +
                            "people who haven't reported anything, can't reach the fake page",
                    "It reveals who the attacker is"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Resetting accounts protects people who are already known victims. Who else " +
                        "might click?",
                "DNS turns a name like cvsu-payroll.example into an address. What happens if it " +
                        "refuses?"
            ),
            successFeedback = "It protects the victims you don't know about yet. 38 staff got the " +
                    "payroll email and hundreds of students got the scholarship one. On campus, " +
                    "the link now leads nowhere. Off campus, on mobile data, it still works, " +
                    "which is why the emails themselves have to go next.",
            failureFeedback = "Not that. Account resets and email clean-up are separate jobs. " +
                    "Think about who might click the link after now."
        ),

        LabTask(
            id = "t4",
            title = "Pull back the bait",
            objective = "The phishing emails are still sitting in students' inboxes. **Purge " +
                    "them**, then **check the click log**. **How many students typed their " +
                    "password** into the fake page?",
            guide = listOf(
                "A blocked domain doesn't help a student on mobile data at home. The phishing " +
                        "email itself must be **removed from every inbox** it reached. Mail " +
                        "systems can **search and purge**: find every copy by sender, subject " +
                        "and link, and pull it back.",
                "Then follow the harm. The web proxy and DNS logs show **who clicked**. A click " +
                        "alone usually isn't a compromise. Anyone who **submitted the form** " +
                        "typed their password into the attacker's page, and their account " +
                        "needs **the same containment** as the staff.",
                "This is how containment works: the **scope grows** as you learn more. Every new " +
                        "victim gets the same treatment."
            ),
            steps = listOf(
                "On the console, tap **MAIL PURGE**.",
                "Check the search finds the right emails, then tap **PURGE FROM ALL MAILBOXES**.",
                "Tap **CLICK LOG**. Separate **clicked** from **submitted the form**.",
                "Tap **CONTAIN STUDENT ACCOUNTS** for the ones who submitted.",
                "**Swipe the bar** down and type **how many** students submitted their password."
            ),
            entryPage = "mail.html",
            requiredClues = listOf(
                ContainmentClues.PURGED,
                ContainmentClues.CLICKS_SEEN
            ),
            lockedMessage = "Purge the emails and open the click log first.",
            answer = LabAnswer.Text(
                accepted = listOf("2", "two"),
                placeholder = "A number"
            ),
            hints = listOf(
                "Six students clicked. Not all of them went further.",
                "Look for the lines marked SUBMITTED."
            ),
            successFeedback = "Two. Six students clicked, two typed their portal password into " +
                    "the fake form. Those two accounts now get the same containment as the " +
                    "staff: sessions revoked, sign-in blocked, password reset. The other four " +
                    "clicked and left, so a warning is enough for them.",
            failureFeedback = "Not quite. Count only the students marked SUBMITTED, not everyone " +
                    "who clicked."
        ),

        LabTask(
            id = "t5",
            title = "Re-entry test",
            objective = "Open the **re-entry test** and run it. The console tries every way back " +
                    "in. **Fix whatever fails**, run it again until every check is green, and " +
                    "submit the **containment code**.",
            guide = listOf(
                "Attackers expect to be found, so they leave themselves a **way back in**. " +
                        "This is called **persistence**: a forwarding rule, an extra login " +
                        "method, a recovery phone number, an app given access to the account.",
                "A common one: the attacker **registers their own MFA device or recovery " +
                        "phone**. After you reset the password, they use **\"Forgot " +
                        "password\"**, approve it on their own device, and choose a new " +
                        "password themselves.",
                "So containment isn't finished when you think the attacker is out. It's " +
                        "finished when you've **tested** it: every session gone, every rule " +
                        "gone, every sign-in method one the **real owner** recognises."
            ),
            steps = listOf(
                "On the console, tap **RE-ENTRY TEST**, then **RUN TEST**.",
                "Read each check. A red one says exactly what the attacker could still do.",
                "Fix it in the right place: the account's page, network blocks or mail purge.",
                "If a sign-in method is the problem, look at the account's **MFA & RECOVERY** " +
                        "section for one the owner never added.",
                "Run the test again. When **every check is green**, a containment code " +
                        "appears. **Swipe the bar** down and type it **exactly as written**."
            ),
            entryPage = "verify.html",
            requiredClues = listOf(ContainmentClues.VERIFY_PASSED),
            lockedMessage = "Pass the re-entry test with every check green first.",
            answer = LabAnswer.Flag(
                sha256 = "e57e4172028df339aa66aac9ca2660bf3152eb137a1e7c89f4b105c24790db8c"
            ),
            hints = listOf(
                "Prof. Ramos has one phone. How many devices are registered on her account?",
                "Check when the extra device was added, and from which IP."
            ),
            successFeedback = "Every check green. The last door was a \"Pixel 7\" registered on " +
                    "Prof. Ramos's account at 2:12 AM Saturday, from the attacker's IP. With it, " +
                    "a self-service password reset would have handed the account straight back. " +
                    "The attacker is out and can't return. Contained, not cleaned: the leaked " +
                    "mail, the student accounts and the policy gaps are Recovery's job.",
            failureFeedback = "Not the containment code. It appears when every check in the " +
                    "re-entry test is green."
        )
    )
)
