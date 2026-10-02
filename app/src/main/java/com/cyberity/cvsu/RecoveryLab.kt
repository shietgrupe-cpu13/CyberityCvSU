package com.cyberity.cvsu

// ===========================================================================
// LEVEL 604 CONTENT — Recovery, Tuesday morning after the breach
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 601-603:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 06/recovery_room.
//
// Closes the case that ran through 601-603. The attacker is out; now the
// accounts go back to the right people, lost data comes back without the
// attacker's changes, hidden business damage is found, the root cause is
// fixed in proportion, and the whole incident is reviewed as one timeline.
// 605 then runs a fresh incident end to end.

/** Clue ids reported by the recovery room through the JS bridge. */
object RecoveryClues {
    const val REQUESTS_DONE = "requests_done"
    const val RESTORE_DONE = "restore_done"
    const val BEC_FOUND = "bec_found"
    const val PAYMENT_HELD = "payment_held"
    const val ROOT_CAUSE = "root_cause"
    const val FIXES_APPLIED = "fixes_applied"
    const val TIMELINE_DONE = "timeline_done"

    /** Dangerous: recovery steps that hand the attacker a way back in. */
    const val APPROVED_IMPOSTOR = "approved_impostor"
    const val RESTORED_BACKDOOR = "restored_backdoor"
    const val CALLED_FAKE_NUMBER = "called_fake_number"
}

val recoveryClueLabels: Map<String, String> = mapOf(
    RecoveryClues.REQUESTS_DONE to "Handled all six account requests correctly",
    RecoveryClues.RESTORE_DONE to "Restored Prof. Ramos's lost mail safely",
    RecoveryClues.BEC_FOUND to "Found the fraudulent payment request",
    RecoveryClues.PAYMENT_HELD to "Held the ₱486,000 payment",
    RecoveryClues.ROOT_CAUSE to "Found how the attacker got past MFA",
    RecoveryClues.FIXES_APPLIED to "Applied the right fixes",
    RecoveryClues.TIMELINE_DONE to "Rebuilt the full incident timeline"
)

fun recoveryLab(): LabDefinition = LabDefinition(
    levelId = 604,
    title = "Close the Hole",
    subtitle = "Recovery · ITSO",
    briefing = "Tuesday, 9:00 AM. The attacker is out and can't get back in. That's " +
            "containment. It isn't recovery.\n\n" +
            "Six people want their accounts back. Prof. Ramos is missing three days of sent " +
            "mail. Nobody has checked what the attacker did inside the other three accounts. " +
            "And nobody yet knows how they got past MFA in the first place, so nothing stops " +
            "the same attack working next week.\n\n" +
            "Recovery means getting back to normal safely: the right people back in, the data " +
            "back without the attacker's changes, the damage found and reversed, and the hole " +
            "closed. Steps that hand the attacker a way back in cost a heart.\n\n" +
            "Each task explains what to look for, then hands you the recovery room. Open it with " +
            "the button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 06/recovery_room",
    startPage = "room.html",
    dangerousClues = listOf(
        RecoveryClues.APPROVED_IMPOSTOR,
        RecoveryClues.RESTORED_BACKDOOR,
        RecoveryClues.CALLED_FAKE_NUMBER
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Prove it's you",
            objective = "Six people are asking for their accounts back. For each, **restore " +
                    "access** or **verify first** through a trusted channel. Then answer **what " +
                    "made the riskiest request so dangerous**.",
            guide = listOf(
                "Handing an account back is the moment the attacker is most likely to try " +
                        "again. They've read the victim's email for days, so they know names, " +
                        "dates and details. **Knowing things is not proof of identity.**",
                "Verify through a **trusted channel**: the person **in person with their ID**, " +
                        "or a **callback to the number already on file**, never to a number the " +
                        "caller gives you. A request from a **personal email** or a **social " +
                        "media message** proves nothing about who sent it.",
                "Never **read out or send a new password** over the phone or by message. The " +
                        "owner sets their own password at the desk or through a verified link."
            ),
            steps = listOf(
                "Open the recovery room and tap **ACCOUNT REQUESTS**.",
                "For each request, read **how they contacted ITSO** and what was **checked**.",
                "Tap **RESTORE ACCESS** only when identity was proven through a trusted channel. " +
                        "Otherwise tap **VERIFY FIRST**.",
                "Restoring the wrong person costs a heart.",
                "Tap **SUBMIT DECISIONS**, then **swipe the bar** down and answer."
            ),
            entryPage = "requests.html",
            requiredClues = listOf(RecoveryClues.REQUESTS_DONE),
            lockedMessage = "Make the right call on all six requests first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "She was calling from Manila instead of campus",
                    "The caller knew details from the stolen mailbox, called from a number not " +
                            "on file, and wanted a new password read out over the phone",
                    "She sounded stressed",
                    "Phone calls can never be used to verify anyone"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Compare the caller's number with the one on file.",
                "Three requests came through channels anyone could fake."
            ),
            successFeedback = "The \"Prof. Ramos\" call. The caller knew about the payroll email " +
                    "and her student assistant, because they'd read her mail for three days. " +
                    "But the number wasn't hers, and she wanted the password read aloud. The " +
                    "real Prof. Ramos came to the desk at 10 AM with her ID.",
            failureFeedback = "Not that. Calling by phone is fine if you call back the number on " +
                    "file. Look at what the caller knew, which number they used, and what they " +
                    "asked for."
        ),

        LabTask(
            id = "t2",
            title = "Restore data, not settings",
            objective = "Prof. Ramos's sent mail from Saturday to Monday was deleted by the " +
                    "attacker's rule. **Preview the restore options** and **restore it " +
                    "safely**. Then answer **why not roll back to Saturday 6 AM**.",
            guide = listOf(
                "Backups are how lost data comes back, but a backup is a **copy of everything " +
                        "at that moment**, including whatever the attacker had changed by then. " +
                        "Restoring the wrong snapshot can **put the attacker straight back " +
                        "in**.",
                "So separate **data** from **settings**. Data (emails, files) can be restored " +
                        "from any point. **Settings** (rules, sign-in methods, permissions) " +
                        "should stay as they are **now**, after containment cleaned them.",
                "And restore **only what's needed**. A full rollback also deletes everything " +
                        "legitimate that arrived after the snapshot."
            ),
            steps = listOf(
                "In the recovery room, tap **MAILBOX RESTORE**.",
                "Tap **PREVIEW** on each option. Read what it brings back **and** what it " +
                        "removes.",
                "Choose the option that brings back **her** sent mail **without** any of the " +
                        "attacker's settings.",
                "Restoring the attacker's changes costs a heart.",
                "**Swipe the bar** down and answer."
            ),
            entryPage = "restore.html",
            requiredClues = listOf(RecoveryClues.RESTORE_DONE),
            lockedMessage = "Restore Prof. Ramos's sent mail safely first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Saturday's backup is too old to be useful",
                    "It would bring back the attacker's forwarding rule and the Pixel 7 " +
                            "recovery phone, along with the mail",
                    "Backups can only be restored on weekends",
                    "It would change her password back"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Open the Saturday 6 AM preview. What settings does it contain?",
                "The rule was created at 2:10 AM and the phone added at 2:12 AM."
            ),
            successFeedback = "The Saturday 6 AM snapshot was taken four hours after the " +
                    "attacker's rule and backdoor phone were added. A full rollback would have " +
                    "undone all of 603. Restoring deleted items only brought back her 31 sent " +
                    "emails and left her clean settings alone.",
            failureFeedback = "Not that. Open the Saturday 6 AM preview and compare its settings " +
                    "with the attack timeline."
        ),

        LabTask(
            id = "t3",
            title = "The damage you didn't see",
            objective = "Check **what the attacker sent** from the other three accounts. Find " +
                    "the request that puts **money** at risk, **stop it the safe way**, and " +
                    "submit the **amount**.",
            guide = listOf(
                "An attacker inside a mailbox doesn't only steal. They **use the trust** that " +
                        "account has. **Business email compromise (BEC)** is when they send " +
                        "requests from a real colleague's account, most often to **change " +
                        "where a payment goes**.",
                "So recovery checks **business impact**, not just IT: what did the attacker " +
                        "**send**, to whom, and has anyone **acted on it** yet?",
                "Verify a payment change **out of band**: call the supplier on the number " +
                        "**already in the vendor records**, never the number in the email. The " +
                        "email is the attacker's, so its phone number is too."
            ),
            steps = listOf(
                "In the recovery room, tap **SENT BY THE ATTACKER**.",
                "Open each account's tab. Read every email the attacker's session sent.",
                "Open the one about **bank details** and read it carefully: amount, due " +
                        "time, contact number.",
                "Tap **HOLD THE PAYMENT**. Don't call the number in the email: it costs a heart.",
                "**Swipe the bar** down and type the **amount** (numbers only is fine)."
            ),
            entryPage = "sent.html",
            requiredClues = listOf(
                RecoveryClues.BEC_FOUND,
                RecoveryClues.PAYMENT_HELD
            ),
            lockedMessage = "Find the payment request and hold the payment first.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "486000", "486,000", "486000.00", "486,000.00",
                    "₱486,000", "₱486000", "₱486,000.00", "php 486,000", "php486,000",
                    "php 486000", "p486,000"
                ),
                placeholder = "e.g. 125000"
            ),
            hints = listOf(
                "It came from the Accounting Office's account.",
                "It's addressed to the Disbursement Office, about a supplier's bank account."
            ),
            successFeedback = "₱486,000, due to go out at 3 PM today to an account the attacker " +
                    "controls. It was sent from a.mendoza in Accounting, so Disbursement had " +
                    "every reason to trust it. The payment is held, and Luzon Print Supply " +
                    "confirmed on their number on file that their bank details never changed.",
            failureFeedback = "Not that amount. Open a.mendoza's sent mail and read the bank " +
                    "details email."
        ),

        LabTask(
            id = "t4",
            title = "Close the hole",
            objective = "Find **how the attacker got past MFA**, then choose the fixes that " +
                    "close the hole **in proportion**. Then answer **what the root cause was**.",
            guide = listOf(
                "Recovery isn't finished until the **root cause** is fixed: the weakness that " +
                        "let the attack work. Otherwise the same email next week gets the same " +
                        "result.",
                "Look for **why the defences didn't hold**. Prof. Ramos had MFA. So how did a " +
                        "password alone get the attacker in? Old mail protocols like **IMAP and " +
                        "POP** (\"legacy authentication\") were built before MFA existed, and " +
                        "many systems let them **skip MFA entirely**.",
                "Fix **in proportion**: close the actual hole and the gaps the attacker used, " +
                        "and plan for the people the fix affects. A fix that breaks everyone's " +
                        "email gets reversed within a week."
            ),
            steps = listOf(
                "In the recovery room, tap **ROOT CAUSE & FIXES**.",
                "Tap **SIGN-IN DETAILS** and read how the attacker's sign-ins were made.",
                "Read each fix: **what it fixes** and **who it affects**.",
                "Select the fixes that close **what this attacker actually used**, and skip " +
                        "the ones that only cause pain.",
                "Tap **APPLY FIXES**, then **swipe the bar** down and answer."
            ),
            entryPage = "fixes.html",
            requiredClues = listOf(
                RecoveryClues.ROOT_CAUSE,
                RecoveryClues.FIXES_APPLIED
            ),
            lockedMessage = "Read the sign-in details and apply the right fixes first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The attacker stole her phone",
                    "MFA was switched off for everyone",
                    "The attacker guessed her MFA codes",
                    "Her account still allowed IMAP, an old mail protocol that never asks for MFA"
                ),
                correctIndex = 3
            ),
            hints = listOf(
                "Look at the protocol column in the sign-in details.",
                "Three fixes match three things the attacker did: get in, forward mail, and " +
                        "redirect a payment."
            ),
            successFeedback = "IMAP. Her MFA worked perfectly in the browser and the Outlook " +
                    "app, but IMAP sign-ins never asked for it, so a stolen password was enough. " +
                    "Legacy sign-in is now off, forwarding outside CvSU needs approval, and bank " +
                    "detail changes need a callback. The 12 staff still on old mail apps are " +
                    "being moved by ITSO this week.",
            failureFeedback = "Not that. Her MFA was on and her phone never left her. Read the " +
                    "protocol on the attacker's sign-ins."
        ),

        LabTask(
            id = "t5",
            title = "Incident closed",
            objective = "Open the **post-incident review**. Tap the eight key events into the " +
                    "**right order**, from the first phishing email to the hole being closed. A " +
                    "correct timeline gives the **case code**. Submit it.",
            guide = listOf(
                "Every incident ends with a **post-incident review** (also called **lessons " +
                        "learned**). It isn't about blame. It's about the **timeline**: what " +
                        "happened, when it was noticed, what was done, and what will change.",
                "An incident response follows a cycle: **prepare**, **detect**, **contain**, " +
                        "**recover**, **learn**. What the review learns feeds back into " +
                        "preparation, like the new alert from 602 and the fixes from this level.",
                "The review also measures the response: **time to detect**, **time to " +
                        "contain**, and **impact**. Those numbers show whether next time is " +
                        "getting better."
            ),
            steps = listOf(
                "In the recovery room, tap **POST-INCIDENT REVIEW**.",
                "Tap the events **in the order they happened**. Each one drops into the next " +
                        "slot on the timeline.",
                "Use **UNDO** if you place one wrong.",
                "When all eight are placed, tap **CHECK TIMELINE**.",
                "When it's right, a **case code** appears. **Swipe the bar** down and type it " +
                        "**exactly as written**."
            ),
            entryPage = "review.html",
            requiredClues = listOf(RecoveryClues.TIMELINE_DONE),
            lockedMessage = "Put all eight events in the right order first.",
            answer = LabAnswer.Flag(
                sha256 = "b2d3ab67868075b1a227f216435c0055379900301daf67d42cea99c343f09910"
            ),
            hints = listOf(
                "The phishing email arrived before anyone clicked it.",
                "The attacker signed in hours before the account sent anything."
            ),
            successFeedback = "Case closed. Phishing on Friday afternoon, detected 63 hours later " +
                    "on Monday morning, contained within the hour, ₱486,000 saved, and the hole " +
                    "it came through sealed. That's the whole incident response cycle. Next: " +
                    "the Incident Response Simulation, where you run a new one from start to " +
                    "finish.",
            failureFeedback = "Not the case code. It appears once all eight events are in the " +
                    "right order."
        )
    )
)
