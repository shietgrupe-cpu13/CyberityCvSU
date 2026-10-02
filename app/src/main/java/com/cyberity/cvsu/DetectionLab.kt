package com.cyberity.cvsu

// ===========================================================================
// LEVEL 602 CONTENT — Detection, rewinding the weekend on the ITSO console
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 601:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 06/detection_console.
//
// Picks up 601's case. Prof. Ramos's account was taken over on Friday and
// nobody noticed until Monday. This level replays the same weekend in the
// monitoring console: true and false positives, reading a sign-in log against
// a baseline, pivoting on an indicator, why a working alert went unseen, and
// tuning a rule that catches the attack without drowning the desk in noise.

/** Clue ids reported by the detection console through the JS bridge. */
object DetectionClues {
    const val ALERTS_COMPLETE = "alerts_complete"
    const val SEARCHED_USER = "searched_user"
    const val BASELINE_RAMOS = "baseline_ramos"
    const val SEARCHED_IP = "searched_ip"
    const val MUTED_RULE = "muted_rule"
    const val RULE_SAVED = "rule_saved"

    /** Dangerous: silencing alerts because there are too many of them. */
    const val DISMISSED_LOW = "dismissed_low"
    const val MUTED_COUNTRY = "muted_country"
}

val detectionClueLabels: Map<String, String> = mapOf(
    DetectionClues.ALERTS_COMPLETE to "Gave all ten weekend alerts the right verdict",
    DetectionClues.SEARCHED_USER to "Pulled Prof. Ramos's sign-in log",
    DetectionClues.BASELINE_RAMOS to "Checked what's normal for her account",
    DetectionClues.SEARCHED_IP to "Pivoted on the attacker's IP address",
    DetectionClues.MUTED_RULE to "Found the alert that was muted in August",
    DetectionClues.RULE_SAVED to "Saved a rule with every attack and no false alarms"
)

fun detectionLab(): LabDefinition = LabDefinition(
    levelId = 602,
    title = "Rewind the Breach",
    subtitle = "Detection · ITSO",
    briefing = "Prof. Ramos typed her password into a fake page on Friday at 4:47 PM. The " +
            "attacker signed in as her at 2:10 AM Saturday. Nobody noticed until a student " +
            "replied to a phishing email on Monday morning: 63 hours later.\n\n" +
            "The ITSO security lead hands you the campus monitoring console, the system that " +
            "collects every log and raises alerts, and asks the question every incident review " +
            "asks: the logs saw it, so why did nobody notice?\n\n" +
            "Replay the weekend. Sort real alerts from false alarms, read the logs, find out " +
            "how far the attacker got, find what was missed, and build the alert that would " +
            "have caught it. Looking is free. Silencing alerts costs a heart.\n\n" +
            "Each task explains what to look for, then hands you the console. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 06/detection_console",
    startPage = "console.html",
    dangerousClues = listOf(
        DetectionClues.DISMISSED_LOW,
        DetectionClues.MUTED_COUNTRY
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Signal or noise?",
            objective = "**Review all ten weekend alerts** and mark each a **real threat** or a " +
                    "**false alarm**. When the queue is right, answer **what it tells you about " +
                    "severity**.",
            guide = listOf(
                "**Detection** is noticing an incident while it is happening, instead of " +
                        "days later. Most of it is done by a monitoring system (a **SIEM**) " +
                        "that collects logs from everything and raises an **alert** when " +
                        "something matches a rule.",
                "An alert is only the tool's **guess**. A **true positive** is an alert about a " +
                        "real threat. A **false positive** is a false alarm: normal activity " +
                        "that happened to look suspicious. A **false negative** is the worst " +
                        "case: a real attack with **no alert at all**.",
                "Too many false alarms cause **alert fatigue**: people stop reading them " +
                        "carefully, and the real one slips through. The **severity** the tool " +
                        "assigns doesn't settle anything. Check each alert against **what is " +
                        "normal** for that user, machine or schedule."
            ),
            steps = listOf(
                "Open the console and tap **ALERT QUEUE**.",
                "Tap **DETAILS** on each alert and read the context: who, from where, and " +
                        "whether it matches a **schedule, ticket or known habit**.",
                "Mark each one **REAL THREAT** or **FALSE ALARM**.",
                "**Don't** use **Dismiss all LOW**. It costs a heart.",
                "Tap **SUBMIT VERDICTS**. The queue says **how many** are right, not which.",
                "**Swipe the bar** down and answer the question about severity."
            ),
            entryPage = "alerts.html",
            requiredClues = listOf(DetectionClues.ALERTS_COMPLETE),
            lockedMessage = "Give all ten alerts the right verdict first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "LOW alerts can be safely ignored on weekends",
                    "Severity is only the tool's guess. Every alert has to be checked against " +
                            "what's normal",
                    "The monitoring tool is broken and should be switched off",
                    "HIGH alerts are always real threats"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Both HIGH alerts match something scheduled or ticketed.",
                "The three real threats all lead back to cvsu-payroll.example or to l.ramos."
            ),
            successFeedback = "Both HIGH alerts were false alarms: a scheduled backup and a " +
                    "ticketed reimage. The attack itself came in as a LOW: a new-country sign-in " +
                    "for l.ramos at 2:10 AM, with three similar alerts quietly suppressed. Every " +
                    "real signal was in the queue all weekend. Nobody was reading it.",
            failureFeedback = "Not that. Look at which severities turned out to be real, and " +
                    "which turned out to be routine."
        ),

        LabTask(
            id = "t2",
            title = "Read the log",
            objective = "**Pull Prof. Ramos's sign-in log** and **check her baseline**, then " +
                    "submit the **IP address** of the sign-in that couldn't have been her.",
            guide = listOf(
                "Every sign-in leaves a line in a log: **when**, **which account**, **from " +
                        "which IP address and location**, **which device**, and whether it " +
                        "**succeeded**. One line means little. The **pattern** tells the story.",
                "Analysts compare activity to a **baseline**: what is normal for this person. " +
                        "Where do they usually sign in from? Which devices? What hours? A " +
                        "sign-in that breaks **several** habits at once deserves a hard look.",
                "The classic giveaway is **impossible travel**: two successful sign-ins from " +
                        "places too far apart for one person to cover in the time between them."
            ),
            steps = listOf(
                "Open the console and tap **SIGN-IN LOGS**.",
                "Search for **l.ramos** (or tap the quick search).",
                "Tap **BASELINE** to see what's normal for her account.",
                "Read the log line by line. Find a sign-in that breaks her baseline, and check " +
                        "her **phone's** sign-in **just before** it.",
                "**Swipe the bar** down and type that sign-in's **IP address**."
            ),
            entryPage = "logs.html",
            requiredClues = listOf(
                DetectionClues.SEARCHED_USER,
                DetectionClues.BASELINE_RAMOS
            ),
            lockedMessage = "Search for l.ramos and open her baseline first.",
            answer = LabAnswer.Text(
                accepted = listOf("203.0.113.47"),
                placeholder = "e.g. 192.0.2.10"
            ),
            hints = listOf(
                "Her phone synced mail from Cavite at 1:55 AM Saturday.",
                "Fifteen minutes later: a new device, in another country."
            ),
            successFeedback = "203.0.113.47. At 1:55 AM her phone synced from Cavite; at 2:10 AM " +
                    "an unknown device signed in from Frankfurt. Nobody flies to Germany in " +
                    "fifteen minutes. A new device, a new country and an hour she never works, " +
                    "all on one line.",
            failureFeedback = "Not that one. That line fits her baseline. Look for the sign-in " +
                    "with a new device and a country she has never signed in from."
        ),

        LabTask(
            id = "t3",
            title = "Pivot",
            objective = "Search the logs for the **attacker's IP address** across **every " +
                    "account**. **How many other staff accounts** did it sign in to " +
                    "successfully?",
            guide = listOf(
                "A clue that marks an attacker, like an IP address, a domain or a file hash, " +
                        "is called an **indicator of compromise (IOC)**. Once you have one, you " +
                        "**pivot**: search for it **everywhere**, not only where you found it.",
                "Attackers rarely stop at one victim. The phishing email in 601 went to **38 " +
                        "staff**. If one typed their password, others may have too, and they " +
                        "may not have noticed anything yet.",
                "Count **successful** sign-ins only. A **failed** attempt means the attacker " +
                        "tried a password that didn't work: worth noting, but that account " +
                        "wasn't entered."
            ),
            steps = listOf(
                "In **SIGN-IN LOGS**, tap the IP address **203.0.113.47** on Prof. Ramos's " +
                        "line, or type it in the search box.",
                "Read every result. Note **which accounts** and whether each was a **success** " +
                        "or a **failure**.",
                "Leave **l.ramos** out of the count: you already know about her.",
                "**Swipe the bar** down and type **how many other accounts** it got into."
            ),
            entryPage = "logs.html",
            requiredClues = listOf(DetectionClues.SEARCHED_IP),
            lockedMessage = "Search the logs for 203.0.113.47 first.",
            answer = LabAnswer.Text(
                accepted = listOf("3", "three"),
                placeholder = "A number"
            ),
            hints = listOf(
                "The results show failures as well as successes.",
                "Four successful accounts in total, including hers."
            ),
            successFeedback = "Three more: r.delacruz, a.mendoza and j.bautista, all between 2:24 " +
                    "and 2:39 AM Saturday, all recipients of the same payroll email, and none of " +
                    "them has reported a thing. One search turned one incident into four. The " +
                    "other 34 attempts failed: those staff never typed their password.",
            failureFeedback = "Not quite. Count only SUCCESS lines, and leave l.ramos out."
        ),

        LabTask(
            id = "t4",
            title = "Why was it missed?",
            objective = "The attacker set up a hidden mail-forwarding rule on all four accounts. " +
                    "**Open the detection rules**, find the one that should have caught it, and " +
                    "decide **why it didn't**.",
            guide = listOf(
                "Detection doesn't only fail when a tool is missing. More often, the tool saw " +
                        "it and **nobody acted**: the alert was **muted**, set to the wrong " +
                        "**severity**, buried among **false alarms**, or sent to an inbox that " +
                        "nobody watches on weekends.",
                "Muting a noisy alert feels like fixing it, but it creates a **blind spot**. " +
                        "The better fix is **tuning**: adding conditions so the rule still " +
                        "fires on the dangerous case and stays quiet on the normal one.",
                "A key measure is **time to detect**: how long an attacker was inside before " +
                        "anyone noticed. Every hour in that gap is an hour of stolen mail, " +
                        "phishing sent, and access spread."
            ),
            steps = listOf(
                "Open the console and tap **DETECTION RULES**.",
                "Read each rule's **status**. Tap the one about **mail forwarding**.",
                "Read its **history**: **who** changed it, **when**, and **why**.",
                "See what it **would have caught** this weekend.",
                "**Don't** mute any other rule. It costs a heart.",
                "**Swipe the bar** down and choose **why it was missed**."
            ),
            entryPage = "rules.html",
            requiredClues = listOf(DetectionClues.MUTED_RULE),
            lockedMessage = "Open the mail forwarding rule's history first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "No tool could have detected a forwarding rule",
                    "Prof. Ramos switched the alert off herself",
                    "The alert existed, but it was muted in August because it was too noisy",
                    "The attacker deleted the logs"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "One rule's status isn't ON.",
                "Read the note left by whoever changed it."
            ),
            successFeedback = "Muted in August: \"30+ alerts a week, all staff forwarding to their " +
                    "own Gmail.\" The rule would have fired four times between 2:10 and 2:40 AM " +
                    "Saturday. Silencing the noise also silenced the attack. The fix isn't " +
                    "turning it back on as it was. It's tuning it.",
            failureFeedback = "Not that. The logs are intact and the rule exists. Read its status " +
                    "and its history."
        ),

        LabTask(
            id = "t5",
            title = "Build the alert",
            objective = "Open the **rule builder** and choose conditions for a new forwarding " +
                    "alert. Test it against a week of logs until it catches **all 4 attacks** " +
                    "with **0 false alarms**, then save it and submit the **rule code**.",
            guide = listOf(
                "A good detection rule is **specific**. Too **broad**, and it fires on " +
                        "everyone's normal habits until someone mutes it again. Too **narrow**, " +
                        "and it misses the attack: a **false negative**.",
                "Build rules from what makes the attack **different** from normal behaviour. " +
                        "Plenty of staff forward mail to their own Gmail. Very few rules also " +
                        "**hide** what they forward. Fewer still are created right after a " +
                        "sign-in from a **country the user has never been in**.",
                "Always **test** a rule against real past logs before switching it on. The " +
                        "aim is every attack caught and as close to **zero false alarms** as " +
                        "you can get."
            ),
            steps = listOf(
                "Open the console and tap **RULE BUILDER**.",
                "Toggle conditions on and off. The counter **re-tests the rule** against last " +
                        "week's 46 forwarding-rule events every time.",
                "Watch both numbers: **attacks caught** (want 4 of 4) and **false alarms** " +
                        "(want 0).",
                "When it's 4 of 4 with 0 false alarms, tap **SAVE RULE**. A **rule code** " +
                        "appears.",
                "**Swipe the bar** down and type it **exactly as written**, braces included."
            ),
            entryPage = "builder.html",
            requiredClues = listOf(DetectionClues.RULE_SAVED),
            lockedMessage = "Save a rule that catches all 4 attacks with 0 false alarms first.",
            answer = LabAnswer.Flag(
                sha256 = "cc61bee617ef27aa5dec1fa7ddb2afb228237c641ae6007f66141d9fbecd28db"
            ),
            hints = listOf(
                "External forwarding alone is what made the old rule so noisy.",
                "What did the attacker's rules do that a staff member's Gmail forward doesn't? " +
                        "And where was the attacker signing in from?"
            ),
            successFeedback = "Rule saved: all four attacks, zero false alarms. It keys on what " +
                    "made the attacker's rules different, like hiding what they forward or being " +
                    "created from a country the user has never been in, not on forwarding " +
                    "itself. Time to detect drops from 63 hours to about a minute. Next: Containment, and what to do " +
                    "in that minute.",
            failureFeedback = "Not the rule code. It appears once the rule is saved with 4 of 4 " +
                    "caught and 0 false alarms."
        )
    )
)
