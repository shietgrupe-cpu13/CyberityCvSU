package com.cyberity.cvsu

// ===========================================================================
// LEVEL 605 CONTENT — Incident Response Simulation, Stipend Day
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 601-604.
// Simulation assets live in assets/simulations/UNIT 06/ir_simulation.
//
// Unit 6's final simulation. A new incident, run end to end by the student as
// incident lead: credential stuffing against the student portal, with stolen
// accounts used to redirect scholarship stipends before a 5 PM payout. The
// guides are shorter than 601-604 on purpose. This is where the unit's habits
// get used without being walked through them. Every action spends time on the
// clock, and the closing scorecard grades the whole response.

/** Clue ids reported by the incident HQ through the JS bridge. */
object IncidentResponseSimClues {
    const val ALERTS_DONE = "alerts_done"
    const val RANGE_SEARCHED = "range_searched"
    const val PAYOUTS_CHECKED = "payouts_checked"
    const val CONTAINED_ALL = "contained_all"
    const val NOTIFIED = "notified"
    const val CAUSE_SEEN = "cause_seen"
    const val FIXES_DONE = "fixes_done"
    const val REPORT_DONE = "report_done"

    /** Dangerous: out of proportion, public, or indistinguishable from phishing. */
    const val CANCEL_ALL = "cancel_all"
    const val PORTAL_OFFLINE = "portal_offline"
    const val FB_POST = "fb_post"
    const val MESSENGER_ASK = "messenger_ask"
}

val incidentResponseSimClueLabels: Map<String, String> = mapOf(
    IncidentResponseSimClues.ALERTS_DONE to "Sorted the alert queue",
    IncidentResponseSimClues.RANGE_SEARCHED to "Pivoted on the attacker's IP range",
    IncidentResponseSimClues.PAYOUTS_CHECKED to "Checked every payout number change",
    IncidentResponseSimClues.CONTAINED_ALL to "Contained the attack before the payout",
    IncidentResponseSimClues.NOTIFIED to "Told exactly the right people",
    IncidentResponseSimClues.CAUSE_SEEN to "Found why the attack worked",
    IncidentResponseSimClues.FIXES_DONE to "Applied the right fixes",
    IncidentResponseSimClues.REPORT_DONE to "Filed a complete incident report"
)

fun incidentResponseSimLab(): LabDefinition = LabDefinition(
    levelId = 605,
    title = "Stipend Day",
    subtitle = "Incident Response Simulation · ITSO",
    briefing = "Thursday, 1:00 PM. Scholarship stipend day. At 5:00 PM the Scholarship Office " +
            "releases ₱4,000 to each of 1,850 scholars, paid to the GCash number in each " +
            "student's portal profile.\n\n" +
            "The alerts on the console have been piling up since Tuesday night. This time " +
            "you're the incident lead. Nobody hands you the case: you detect it, scope it, " +
            "contain it, tell the right people, find the cause and write it up.\n\n" +
            "Every action spends time on the clock. If the payout goes out before the " +
            "attack is contained, the money goes with it. Moves that are out of proportion, " +
            "public, or look like phishing cost a heart.\n\n" +
            "Everything from 601 to 604 applies. Open the incident HQ with the button, and " +
            "swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 06/ir_simulation",
    startPage = "hq.html",
    dangerousClues = listOf(
        IncidentResponseSimClues.CANCEL_ALL,
        IncidentResponseSimClues.PORTAL_OFFLINE,
        IncidentResponseSimClues.FB_POST,
        IncidentResponseSimClues.MESSENGER_ASK
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Detect",
            objective = "Sort the **alert queue**: real threat or noise. Then name **the kind of " +
                    "attack** the real alerts add up to.",
            guide = listOf(
                "**Credential stuffing** is when an attacker takes usernames and passwords " +
                        "leaked from **another site's breach** and tries them on yours, " +
                        "automatically, thousands at a time. It works because people **reuse " +
                        "passwords**. Most attempts fail. The few that succeed are real " +
                        "accounts with real owners.",
                "Its signature is a **flood of failed sign-ins across many accounts** from a " +
                        "small set of addresses, a **handful of successes** hidden inside it, " +
                        "and then whatever the attacker came for."
            ),
            steps = listOf(
                "Open the HQ and tap **ALERT QUEUE**.",
                "Open each alert's **details**. Mark **REAL THREAT** or **NOISE**.",
                "Tap **SUBMIT VERDICTS**.",
                "**Swipe the bar** down and name the attack."
            ),
            entryPage = "alerts.html",
            requiredClues = listOf(IncidentResponseSimClues.ALERTS_DONE),
            lockedMessage = "Get every alert verdict right first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Credential stuffing: passwords leaked elsewhere, tried on portal accounts at " +
                            "scale",
                    "Brute force: one account's password guessed by trying every combination",
                    "DDoS: the portal flooded with sign-in traffic to knock it offline",
                    "Phishing: scholars tricked into typing their passwords into a fake portal"
                ),
                correctIndex = 0
            ),
            hints = listOf(
                "Three alerts started on Tuesday night and share one IP range.",
                "Thousands of failures, a few dozen successes, then GCash numbers change."
            ),
            successFeedback = "Credential stuffing. 41,000 failed sign-ins across almost 4,000 " +
                    "accounts, 57 successes from one hosting provider's range, and payout " +
                    "numbers changing on stipend week. The CPU spike, the new admin laptop, the " +
                    "certificate notice and the payroll export were all routine.",
            failureFeedback = "Not that. Look at what the real alerts have in common: failed " +
                    "sign-ins at scale, a few successes, then payout changes."
        ),

        LabTask(
            id = "t2",
            title = "Scope",
            objective = "Pivot on the **attacker's IP range** and check **every payout number " +
                    "change** this week. How much stipend money is **at risk**?",
            guide = listOf(
                "Scope means answering: **how many accounts**, **which ones**, and **what was " +
                        "done** to them. Pivot on the indicator you have, the IP range, across " +
                        "**everything**.",
                "Don't assume every matching event is the attacker. Compare each one to the " +
                        "**owner's baseline**: their usual device, place and habits. A real " +
                        "person can do the same thing for a legitimate reason."
            ),
            steps = listOf(
                "Tap **LOGS & SEARCH**. Search the range **192.0.2.0/24**.",
                "Then open **PAYOUT NUMBER CHANGES** and read all of them, including **where " +
                        "each change came from**.",
                "Count the changes made **by the attacker** only.",
                "Each scholar receives **₱4,000**. **Swipe the bar** down and type the total " +
                        "at risk."
            ),
            entryPage = "logs.html",
            requiredClues = listOf(
                IncidentResponseSimClues.RANGE_SEARCHED,
                IncidentResponseSimClues.PAYOUTS_CHECKED
            ),
            lockedMessage = "Search the attacker's range and check the payout changes first.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "92000", "92,000", "92000.00", "92,000.00",
                    "₱92,000", "₱92000", "₱92,000.00", "php 92,000", "php92,000", "php 92000"
                ),
                placeholder = "e.g. 48000"
            ),
            hints = listOf(
                "24 numbers changed this week. Were all 24 changed from the attacker's range?",
                "One change came from the student's own phone, on campus, with a help desk ticket."
            ),
            successFeedback = "₱92,000: 23 payout numbers changed from 192.0.2.0/24. The 24th, " +
                    "student 202312045, changed her own number from her usual phone on campus " +
                    "after losing her old SIM. Same event, different owner. The attacker got " +
                    "into 57 accounts in total, so 34 of them are compromised even though the " +
                    "money wasn't touched.",
            failureFeedback = "Not quite. Count only the changes from the attacker's range, then " +
                    "multiply by ₱4,000."
        ),

        LabTask(
            id = "t3",
            title = "Contain before 5 PM",
            objective = "Use the containment panel to **stop the attack and protect the payout** " +
                    "before 5:00 PM. Then answer **why you held only the changed payouts**.",
            guide = listOf(
                "Ask what is **still causing harm**, and stop that first, in the **right " +
                        "order**. If more numbers can still change while you build a hold list, " +
                        "the list is out of date before you apply it.",
                "Keep it **proportionate**. 1,826 scholars did nothing wrong and are counting " +
                        "on today's stipend. The portal is mid-enrollment. Public posts help " +
                        "the attacker more than the victims."
            ),
            steps = listOf(
                "Tap **CONTAIN**. Watch the clock and the **₱ at risk** meter.",
                "Stop **new** payout changes before deciding which payouts to hold.",
                "Cut the attacker off from the accounts **and** from the portal.",
                "Avoid anything that hurts everyone. Those cost a heart.",
                "**Swipe the bar** down and answer."
            ),
            entryPage = "contain.html",
            requiredClues = listOf(IncidentResponseSimClues.CONTAINED_ALL),
            lockedMessage = "Apply all four containment steps first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Holding all 1,850 payouts needs written approval from the Scholarship Office",
                    "Those 24 accounts are the only ones that signed in from the attacker's range",
                    "Only a changed payout number can send money to the attacker; the rest are safe",
                    "Holding fewer payouts keeps the incident small enough to stay unreported"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Which payouts could actually reach the attacker?",
                "Containment is the smallest action that fully works."
            ),
            successFeedback = "Exactly. Only a changed payout number can send money to the " +
                    "attacker. Holding those 24 protects all ₱92,000 while 1,826 scholars get " +
                    "their stipend at 5 PM as promised. Student 202312045's payment is held " +
                    "too, until the desk verifies her in person, then released.",
            failureFeedback = "Not that. Think about which payouts could actually reach the " +
                    "attacker, and who else is waiting for their stipend today."
        ),

        LabTask(
            id = "t4",
            title = "Tell the right people",
            objective = "Choose **who to notify** and send. Then answer **the deadline** that " +
                    "applies if the DPO decides the breach must be reported.",
            guide = listOf(
                "Tell **the people who must act**: whoever protects the data, whoever runs " +
                        "the affected process, the victims, and anyone who can stop the money " +
                        "at the other end.",
                "Reach victims through **official channels** that they can verify. Never send " +
                        "a message that **looks like the attack**: if ITSO asks students to " +
                        "confirm their GCash number on Messenger, the next scammer will too."
            ),
            steps = listOf(
                "Tap **NOTIFY**.",
                "Select **everyone who needs to act**, and nobody who would only spread panic.",
                "Tap **SEND NOTIFICATIONS**. One option costs a heart.",
                "**Swipe the bar** down and answer."
            ),
            entryPage = "notify.html",
            requiredClues = listOf(IncidentResponseSimClues.NOTIFIED),
            lockedMessage = "Send the right set of notifications first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Within 30 days, once the DPO has finished investigating the breach",
                    "Within 72 hours of money being lost, if any stipend was paid out",
                    "Within 5 working days of the affected students being notified",
                    "Within 72 hours of the University first knowing about the breach"
                ),
                correctIndex = 3
            ),
            hints = listOf(
                "This is the same rule as the misdirected OJT email in 601.",
                "The clock starts when the University knows, not when the money moves."
            ),
            successFeedback = "Within 72 hours. Portal profiles hold birthdays, addresses and " +
                    "student numbers, so the DPO assesses it today. The Scholarship Office " +
                    "knows why 24 payouts are held, the 57 students get an official notice, and " +
                    "the e-wallet provider has flagged the 23 attacker numbers.",
            failureFeedback = "Not that. It's the Data Privacy Act deadline from 601."
        ),

        LabTask(
            id = "t5",
            title = "Root cause",
            objective = "Read the **portal's security settings** and the **password check**, then " +
                    "apply the fixes that **close the hole in proportion**. Then name the **root " +
                    "cause**.",
            guide = listOf(
                "Credential stuffing needs two things: **reused passwords**, and a site that " +
                        "**lets an attacker try thousands of them**. Remove either and it fails.",
                "Fix what the attacker actually used, and protect **what they were after**. " +
                        "Changing where money goes should never rely on a password alone."
            ),
            steps = listOf(
                "Tap **ROOT CAUSE**, then **PORTAL SECURITY**.",
                "Select the fixes that would have stopped **this** attack.",
                "Tap **APPLY FIXES**. The board says how many choices are right.",
                "**Swipe the bar** down and name the root cause."
            ),
            entryPage = "cause.html",
            requiredClues = listOf(
                IncidentResponseSimClues.CAUSE_SEEN,
                IncidentResponseSimClues.FIXES_DONE
            ),
            lockedMessage = "Read the portal security settings and apply the right fixes first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Passwords reused from another site's leak, and no limit on sign-in attempts",
                    "Passwords stolen from CvSU's own portal database in a separate breach",
                    "Weak passwords, like birthdays, that the attacker could simply guess",
                    "Students sharing their portal passwords with classmates and org officers"
                ),
                correctIndex = 0
            ),
            hints = listOf(
                "Where did the working passwords come from?",
                "Look at the rate limit and MFA settings."
            ),
            successFeedback = "Reuse plus no limits. 49 of the 57 stolen passwords appear in the " +
                    "ShopZone leak, and the portal let the attacker try 41,000 sign-ins without " +
                    "slowing down or asking for anything else. Now sign-ins are rate-limited, " +
                    "known leaked passwords are refused, and a payout change needs a one-time " +
                    "code.",
            failureFeedback = "Not that. CvSU's database wasn't breached. Look at where the " +
                    "passwords came from and what the portal allowed."
        ),

        LabTask(
            id = "t6",
            title = "Write-up",
            objective = "Fill in the **incident report** and submit it. A correct report gives " +
                    "the **case code** and your **scorecard**. Submit the code.",
            guide = listOf(
                "The report hands the case to everyone who comes after you: the DPO, the " +
                        "Scholarship Office, the director. **Facts, not guesses**: when it " +
                        "started, when it was discovered, how many, how much, what was done.",
                "Be precise about the **edge cases**. One of the 24 payout changes wasn't the " +
                        "attacker. The report has to say so."
            ),
            steps = listOf(
                "Tap **WRITE-UP**.",
                "Fill in every field from what you found.",
                "Tap **SUBMIT REPORT**. It says how many fields are right.",
                "When it's right, read your scorecard, then **swipe the bar** down and type " +
                        "the **case code** exactly as written."
            ),
            entryPage = "report.html",
            requiredClues = listOf(IncidentResponseSimClues.REPORT_DONE),
            lockedMessage = "Submit a complete, correct report first.",
            answer = LabAnswer.Flag(
                sha256 = "432a278a2d4485095e0953d3f5acca93926435fdf25b8f6fbfe0abbd4c67e1dd"
            ),
            hints = listOf(
                "The first failed sign-in from the range was Tuesday night. You started at 1 PM " +
                        "Thursday.",
                "57 accounts entered, 23 payouts redirected, 1 genuine change."
            ),
            successFeedback = "Case closed, incident lead. Detected, scoped, contained, " +
                    "reported, fixed and written up: the whole cycle, on a case nobody walked " +
                    "you through. That's Unit 6.",
            failureFeedback = "Not the case code. It appears once every field in the report is " +
                    "right."
        )
    )
)
