package com.cyberity.cvsu

/** Shared entry for the welcome banner and both settings menus. */
fun tutorialLevel() = LearningLevel(
    100, "Level 0: CYBERITY Tutorial",
    "Learn how CYBERITY works before starting your cybersecurity training.",
    25, LevelType.LESSON, LevelStatus.CURRENT, durationMinutes = 5
)

// ===========================================================================
// LEVEL 0 CONTENT — a practice lab that teaches how every level is played
// ===========================================================================
// Runs on the real LabScreen (see LevelZeroTutorialScreen.kt), so what the
// player learns here is exactly the screen they will use in every later level.

object TutorialClues {
    const val EMAIL_OPENED_LIBRARY = "email_opened_library"
    const val EMAIL_OPENED_PORTAL = "email_opened_portal"
    const val SENDER_INSPECTED = "sender_inspected"
    const val LINK_FOLLOWED = "link_followed"
    const val PAGE_INFO_OPENED = "page_info_opened"
    const val SOURCE_VIEWED = "source_viewed"
    const val CREDENTIALS_SUBMITTED = "credentials_submitted"
}

val tutorialClueLabels: Map<String, String> = mapOf(
    TutorialClues.EMAIL_OPENED_LIBRARY to "Opened the Library message",
    TutorialClues.EMAIL_OPENED_PORTAL to "Opened the Student Portal message",
    TutorialClues.SENDER_INSPECTED to "Inspected the sender address",
    TutorialClues.LINK_FOLLOWED to "Followed the sign-in link",
    TutorialClues.PAGE_INFO_OPENED to "Opened page details",
    TutorialClues.SOURCE_VIEWED to "Read the page source",
    TutorialClues.CREDENTIALS_SUBMITTED to "Signed in to the fake portal"
)

/**
 * The spotlight tips that walk the player around the lab screen. Each moment
 * plays once; the player can skip any of them.
 */
fun tutorialCoach(): Map<CoachMoment, List<CoachStep>> = mapOf(

    // First time the task screen appears: what everything on it is for.
    CoachMoment.INTRO to listOf(
        CoachStep(
            CoachTarget.HEARTS, "Your hearts",
            "A wrong answer, or a dangerous action like typing into a fake login page, " +
                    "costs one heart. Run out and you wait for them to refill before you can " +
                    "play again. Here they're practice hearts, so don't be afraid to slip up."
        ),
        CoachStep(
            CoachTarget.XP, "Your XP",
            "This is your XP balance. You earn XP by finishing levels, and you spend it on " +
                    "hints. Everyone starts with $STARTING_XP."
        ),
        CoachStep(
            CoachTarget.PROGRESS, "Your progress",
            "Tasks solved out of the total. The bar under the status area fills up as you " +
                    "clear each task. This lab has 3."
        ),
        CoachStep(
            CoachTarget.GUIDE, "1. Read the guide",
            "Every task starts with a short guide. It teaches the idea behind the task and " +
                    "what to look for — it's the lesson. Read it before you touch anything."
        ),
        CoachStep(
            CoachTarget.OBJECTIVE, "2. Know your objective",
            "This card is the one thing the task is asking you to find or decide. Keep it " +
                    "in mind while you investigate."
        ),
        CoachStep(
            CoachTarget.STEPS, "3. Follow the steps",
            "\"How to do it\" lists exactly what to tap, in order. If you're ever unsure " +
                    "what to do next, work down this list."
        ),
        CoachStep(
            CoachTarget.OPEN_SIM, "4. Open the simulation",
            "Tap OPEN SIMULATION to step into the live scenario. Tap around, open things and " +
                    "read them. When you're done, swipe the bar at the top of the simulation " +
                    "down to come back here."
        ),
        CoachStep(
            CoachTarget.EVIDENCE, "5. Collect evidence",
            "Everything you open or tap in the simulation is logged here. Finding the right " +
                    "evidence is what unlocks the answer."
        ),
        CoachStep(
            CoachTarget.HINT, "Stuck? Take a hint",
            "A hint nudges you in the right direction without giving the answer away. It's " +
                    "paid for from your XP: ${nextHintCost(0)} for the first on a task, " +
                    "${nextHintCost(1)} for the second, ${nextHintCost(2)} after that. Using " +
                    "any hint also forfeits the no-hints bonus, so save them for when you " +
                    "really need one."
        ),
        CoachStep(
            CoachTarget.ANSWER, "6. Submit your answer",
            "The answer box stays locked until you've found enough evidence. Once it opens, " +
                    "pick or type your answer and submit it. Right: on to the next task. " +
                    "Wrong: you lose a heart and try again."
        )
    ),

    // First task that asks for a flag.
    CoachMoment.FLAG to listOf(
        CoachStep(
            CoachTarget.OBJECTIVE, "This task wants a flag",
            "A flag is a secret string hidden somewhere inside the system you're " +
                    "investigating — a comment in a page, a line in a log, a field in a file. " +
                    "Finding it proves you really got in and looked. Every flag looks like " +
                    "CYBERITY{some_words_here}."
        ),
        CoachStep(
            CoachTarget.ANSWER, "Capturing it",
            "Once your evidence unlocks this box, type the flag exactly as you found it — " +
                    "CYBERITY and the curly braces included — then tap SUBMIT FLAG. You can't " +
                    "guess a flag; it only turns up by investigating."
        ),
        CoachStep(
            CoachTarget.HEARTS, "Look, don't touch",
            "Some actions inside a simulation are traps, like signing in to a fake page. " +
                    "Those cost a heart straight away. Investigate everything, trust nothing."
        )
    ),

    // Result screen: how the XP was worked out.
    CoachMoment.RESULT to listOf(
        CoachStep(
            CoachTarget.XP_BREAKDOWN, "How your XP is worked out",
            "Finishing a level pays $LEVEL_XP XP, plus bonuses: +$BONUS_NO_HEART_LOST for " +
                    "losing no hearts, +$BONUS_ALL_CLUES for finding all the evidence and " +
                    "+$BONUS_NO_HINTS for using no hints. A perfect run earns $MAX_LEVEL_XP. " +
                    "A bonus you missed shows as +0, so you can see what to go for next time."
        ),
        CoachStep(
            CoachTarget.FINISH, "Ready for the real thing",
            "That's everything. Tap BACK TO PATH and start Level 1 — it works exactly like " +
                    "this one. Good luck, analyst."
        )
    )
)

fun tutorialLab(): LabDefinition = LabDefinition(
    levelId = 100,
    title = "Training Lab",
    subtitle = "Level 0 · Tutorial",
    briefing = "Welcome to CYBERITY. You have just joined the CvSU Security Desk as a junior " +
            "analyst. Threats don't announce themselves — they arrive as ordinary-looking " +
            "messages and pages, and your job is to investigate them instead of guessing.\n\n" +
            "This is a practice run of the real thing. Every level in the game works the same way:\n\n" +
            "1.  Read the task. It teaches you what to look for.\n" +
            "2.  Open the simulation and investigate. Whatever you find is logged as evidence.\n" +
            "3.  Evidence unlocks the answer box. You can't answer until you've looked.\n" +
            "4.  Submit your answer — or capture a flag.\n\n" +
            "Hearts, hints and XP are real mechanics here too, but in this lab they are for " +
            "practice only, so nothing you do can hurt your progress.",
    assetDir = "tutorial",
    startPage = "mailbox.html",
    dangerousClues = listOf(TutorialClues.CREDENTIALS_SUBMITTED),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Investigate, then answer",
            objective = "Two messages are waiting in the mailbox. **Open both** and decide " +
                    "which one is the **phishing attempt**.",
            guide = listOf(
                "This is the screen you'll see in every task. Up top is your status bar: " +
                        "**hearts** on the left (a wrong answer costs one), your **XP balance** " +
                        "beside them, and a **progress bar** that fills as you solve tasks.",
                "Below it is the task itself. The simulation is a live, safe copy of whatever " +
                        "you're investigating — a mailbox, a website, a log. It opens from the " +
                        "**OPEN SIMULATION** button and slides away when you **swipe its top bar " +
                        "down**.",
                "Look at the line that says \"No evidence yet\". Everything you open or tap in " +
                        "the simulation shows up there, and the **answer box stays locked** " +
                        "until you've found enough. That is what makes this a game of " +
                        "**investigation rather than guessing**."
            ),
            steps = listOf(
                "Tap **OPEN SIMULATION**. You'll see the mailbox with two unread messages.",
                "Tap a message to read it, then tap **← Inbox** to go back. Open **both**.",
                "Ask what each message wants you to **DO**. Ordinary mail informs you; " +
                        "phishing **pressures** you.",
                "**Swipe the bar at the top** of the simulation down to return here. Watch the " +
                        "evidence line update, and the answer box unlock.",
                "Tap the option you believe is right, then tap **SUBMIT ANSWER**."
            ),
            entryPage = "mailbox.html",
            requiredClues = listOf(
                TutorialClues.EMAIL_OPENED_LIBRARY,
                TutorialClues.EMAIL_OPENED_PORTAL
            ),
            lockedMessage = "Locked — open both messages in the simulation first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Library Services",
                    "CvSU Student Portal",
                    "Neither — both look safe"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "One message just tells you the library's opening hours. The other threatens " +
                        "you with a 2-hour deadline."
            ),
            successFeedback = "Correct. A deadline plus a threat — \"your enrollment will be " +
                    "locked\" — is manufactured urgency, and its whole job is to stop you " +
                    "checking. You investigated first and answered second: that's the game.",
            failureFeedback = "Not that one. In a real level this costs a heart — your hearts " +
                    "here are practice and refill, so try again. Compare what each message " +
                    "asks you to do."
        ),

        LabTask(
            id = "t2",
            title = "Dig deeper for the answer",
            objective = "The display name says \"CvSU Student Portal\". **Inspect the sender** " +
                    "and type the **domain** the message was really sent from.",
            guide = listOf(
                "Not every task is multiple choice. Sometimes you have to **find a piece of " +
                        "information yourself and type it in**. Answers are not case-sensitive, " +
                        "but they do need to be **spelled exactly** as you found them.",
                "An email address has two parts. The **display name** is free text, so an " +
                        "attacker can write anything there. The **domain after the @** is the " +
                        "part they have to actually own — so it's the only part worth trusting.",
                "**Hints are your safety net.** Tap \"Need a hint?\" below the objective if you " +
                        "get stuck. The first one costs **15 XP** from your balance, and the " +
                        "price rises with each extra hint on the same task. Your balance is " +
                        "shown in the status bar, so you can see it drop."
            ),
            steps = listOf(
                "Open the simulation and open the **\"URGENT: Password expires\"** message.",
                "Tap the **From** row. It expands to show the real sending address.",
                "Read the part **after the @**. Does it match cvsu.edu.ph?",
                "Swipe back down, type the domain into the box, and tap **SUBMIT ANSWER**."
            ),
            entryPage = "mailbox.html",
            requiredClues = listOf(TutorialClues.SENDER_INSPECTED),
            lockedMessage = "Locked — tap the From row inside the message to reveal the real address.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "cvsu-verify.example",
                    "@cvsu-verify.example",
                    "support@cvsu-verify.example"
                ),
                placeholder = "e.g. example-domain.com"
            ),
            hints = listOf(
                "The display name is free text. The domain after the @ is not.",
                "It starts with cvsu-, but it doesn't end with .edu.ph."
            ),
            successFeedback = "Right — cvsu-verify.example. It borrows the university's name " +
                    "but isn't the university's domain. Display names are decoration; the " +
                    "domain is the part an attacker can't fake.",
            failureFeedback = "That isn't the sending domain. Expand the From row and copy the " +
                    "part after the @ exactly as it's written."
        ),

        LabTask(
            id = "t3",
            title = "Capture the flag",
            objective = "The attacker left something behind in the fake sign-in page. Read the " +
                    "**page source** and submit the **flag** you find.",
            guide = listOf(
                "Here is the part that gives cybersecurity games their name. A **flag** is a " +
                        "**secret string hidden** somewhere in the system you're investigating. " +
                        "Finding it proves you got there. It always looks like " +
                        "**CYBERITY{some_words_here}**, and you submit it **in full**.",
                "Attackers build fake sign-in pages from reusable kits, and kits carry debris: " +
                        "**developer comments** left in the page's code. A browser never draws " +
                        "those comments, but they ship with the page — so **reading the " +
                        "source** is how analysts find them.",
                "One rule applies to every level: **investigating** a suspicious page is " +
                        "safe, **signing in** to one is not. Typing into the fake login form " +
                        "**costs a heart**, just as it would cost you an account in real life."
            ),
            steps = listOf(
                "Open the simulation, open the portal message, and tap **SIGN IN NOW** to " +
                        "follow the link into the sandboxed browser.",
                "**Do NOT fill in the form.** Tap the **ⓘ** button at the top to open Page " +
                        "details.",
                "Tap **\"View page source\"** at the bottom of that panel.",
                "Scan the **highlighted comment line** — that's the part a browser never shows.",
                "Swipe back down, type the **whole flag**, CYBERITY{...} included, and tap " +
                        "**SUBMIT FLAG**."
            ),
            entryPage = "mailbox.html",
            requiredClues = listOf(TutorialClues.SOURCE_VIEWED),
            lockedMessage = "Locked — open Page details, then View page source, inside the sandboxed browser.",
            answer = LabAnswer.Flag(
                sha256 = "3cd9e5b997cba27a1f1f7e6729c34b22f5f23eb247227dadccea5fc4270dc249"
            ),
            hints = listOf(
                "Page details has a button most people never press.",
                "Comments in a page's code never render on screen — but they're still there."
            ),
            successFeedback = "Flag captured! Kits carry fingerprints — comments, template " +
                    "names, staging paths — and that debris is how analysts link one attack " +
                    "to another. You now know every move the game asks of you.",
            failureFeedback = "That's not the flag. It's written in full inside the page " +
                    "source, wrapper included: CYBERITY{...}."
        )
    )
)
