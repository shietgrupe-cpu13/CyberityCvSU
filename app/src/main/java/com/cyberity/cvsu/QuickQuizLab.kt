package com.cyberity.cvsu

// ===========================================================================
// LEVEL 350 CONTENT — Quick Quiz, sixty seconds against the clock
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt.
//
// This is recall, not investigation: six questions drawn from 301-304, one
// countdown of sixty seconds for the whole set. The clock lives inside the
// simulation, so the level reuses the lab engine unchanged — the single task
// simply accepts the pass code the quiz issues.
//
// Deliberately gentle on consequences and strict on time: retries are free and
// cost no hearts, because a speed test should punish hesitation, not a slow
// reader's heart count. Hearts stay reserved for dangerous actions in the labs.

/** Clue ids reported by the quiz through the JS bridge. */
object QuickQuizClues {
    const val QUIZ_ATTEMPTED = "quiz_attempted"
    const val QUIZ_PASSED = "quiz_passed"
}

val quickQuizClueLabels: Map<String, String> = mapOf(
    QuickQuizClues.QUIZ_ATTEMPTED to "Ran the sixty-second quiz",
    QuickQuizClues.QUIZ_PASSED to "Scored 5 or better before the clock ran out"
)

fun quickQuizLab(): LabDefinition = LabDefinition(
    levelId = 350,
    title = "Sixty Seconds",
    subtitle = "Quick Quiz · everything from Unit 3",
    briefing = "Six questions, sixty seconds, one clock for the whole set. Nothing here is " +
            "new — every answer came up somewhere in 301 to 304.\n\n" +
            "The time limit is the point. On a real phone you get a few seconds to judge a " +
            "message before you have already tapped it, so this is practice at recognising " +
            "the shape of an attack rather than reasoning your way to it. Spend longer on a " +
            "hard one if you like; the clock does not reset between questions.\n\n" +
            "Five correct out of six passes. Running out of time or missing the mark costs " +
            "you nothing — no hearts, and you can restart as often as you want. Open the " +
            "quiz with the button, and swipe the bar at the top of it down when you have the " +
            "pass code.",
    assetDir = "quick_quiz",
    startPage = "quiz.html",
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Beat the clock",
            objective = "Run the quiz and score at least 5 of 6 before the sixty seconds are " +
                    "up. Submit the pass code it gives you.",
            guide = listOf(
                "Speed changes what you are actually testing. Given a minute per question you " +
                        "would reason each one out from first principles, which is a useful " +
                        "skill and not the one that saves you in real life. Given ten, you have " +
                        "to recognise the pattern — and recognition is what you build by " +
                        "meeting the same shapes repeatedly, which is exactly what Unit 3 was.",
                "There are four shapes worth holding on to. Where a link really goes is " +
                        "decided at the right-hand end of the address, just before the first " +
                        "single slash. A name on a message — sender ID, display name, From " +
                        "line — is typed by the sender and verifies nothing. Any request to " +
                        "send value or secrets first is the scam itself, whatever the story " +
                        "around it. And nobody legitimate will ever ask you for an OTP.",
                "If you run out of time, look at what the results screen shows you before you " +
                        "restart: it marks every question and names the answer you missed. Two " +
                        "runs with the results read in between is worth more than six runs " +
                        "guessing faster."
            ),
            steps = listOf(
                "Open the quiz and tap START when you are ready — the clock begins on that tap.",
                "Answer each question by tapping an option. There is no confirm step, so read " +
                        "before you tap.",
                "Do not wait for feedback between questions; it all comes at the end.",
                "Read the results screen, including the answers you missed.",
                "At 5 or better the pass code appears. Swipe down and type it in with the braces."
            ),
            entryPage = "quiz.html",
            requiredClues = listOf(QuickQuizClues.QUIZ_PASSED),
            lockedMessage = "Score at least 5 of 6 within the sixty seconds to get the pass code.",
            answer = LabAnswer.Flag(
                sha256 = "852a0eabaccdf6b9eb2b178259e1281201beb2480364ee48ba61b8294e0218c4"
            ),
            hints = listOf(
                "Restarting is free and costs no hearts. Read the results screen first — it " +
                        "names every answer you missed.",
                "The pass code only appears at 5 or 6 correct, and it appears on the results " +
                        "screen under your score."
            ),
            successFeedback = "That is Unit 3 closed. You can read a domain right to left, you " +
                    "know a sender's name proves nothing, you spot pay-to-receive for what it " +
                    "is, and you will never read an OTP to anyone. The last one alone will " +
                    "save somebody you know.",
            failureFeedback = "Not the pass code. It appears on the results screen once you " +
                    "score 5 or better inside the sixty seconds."
        )
    )
)
