package com.cyberity.cvsu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===========================================================================
// 1. SIMULATION MODEL
// ===========================================================================

@Immutable
data class SimChoice(
    val text: String,
    val isSafe: Boolean,
    /** Shown after the user commits — explains the consequence of this choice. */
    val consequence: String
)

@Immutable
data class SimScenario(
    val id: Int,
    /** Short setting label, e.g. "CAMPUS LIBRARY". */
    val setting: String,
    val situation: String,
    val question: String,
    val choices: List<SimChoice>,
    /** The security concept this scenario teaches, revealed after answering. */
    val conceptName: String,
    val conceptExplanation: String
)

@Immutable
data class ScenarioQuiz(
    val levelId: Int,
    val title: String,
    val briefing: String,
    val scenarios: List<SimScenario>
)

// ===========================================================================
// 2. LEVEL 1 CONTENT — "Spot the Threat"
// ===========================================================================

fun spotTheThreatQuiz(): ScenarioQuiz = ScenarioQuiz(
    levelId = 101,
    title = "Spot the Threat",
    briefing = "Five everyday campus situations. Choose what you'd actually do — " +
            "you'll see the consequence either way.",
    scenarios = listOf(
        SimScenario(
            id = 1,
            setting = "UNIVERSITY LIBRARY",
            situation = "You find an unlabeled USB flash drive on a study table. Someone has " +
                    "written \"SCHOLARSHIP LIST 2026\" on it with a marker.",
            question = "What do you do?",
            choices = listOf(
                SimChoice(
                    "Plug it into your laptop to find the owner",
                    isSafe = false,
                    consequence = "The drive auto-runs a payload the moment it mounts. Your laptop " +
                            "is now compromised — and it's connected to the campus network."
                ),
                SimChoice(
                    "Hand it to the library staff or the IT office",
                    isSafe = true,
                    consequence = "Correct. Staff can handle it safely, and if it is malicious, " +
                            "IT learns someone is running a drop attack on campus."
                ),
                SimChoice(
                    "Plug it into a lab computer instead of your own",
                    isSafe = false,
                    consequence = "Worse. Shared lab machines reach more students and more of the " +
                            "network than your personal laptop would."
                )
            ),
            conceptName = "USB Drop Attack",
            conceptExplanation = "Attackers deliberately leave infected drives where curious people " +
                    "will find them. The enticing label is the bait — curiosity is the exploit."
        ),
        SimScenario(
            id = 2,
            setting = "YOUR INBOX",
            situation = "An email arrives: \"CvSU Student Portal — your account will be DELETED in " +
                    "24 hours. Verify now.\" The sender address is cvsu-portal-verify@gmail.com.",
            question = "What's your move?",
            choices = listOf(
                SimChoice(
                    "Click the link and log in to save your account",
                    isSafe = false,
                    consequence = "The page was a convincing fake. Your portal credentials were " +
                            "captured the instant you typed them."
                ),
                SimChoice(
                    "Check the sender domain, then report it to IT",
                    isSafe = true,
                    consequence = "Correct. A real university notice never comes from a gmail.com " +
                            "address. Reporting it protects everyone else too."
                ),
                SimChoice(
                    "Forward it to your classmates to warn them",
                    isSafe = false,
                    consequence = "Well-meant, but you just spread a live phishing link to more " +
                            "people — some of whom will click it."
                )
            ),
            conceptName = "Phishing & Urgency Pressure",
            conceptExplanation = "Artificial deadlines exist to stop you from thinking. Slow down " +
                    "and verify the sender before you act on any urgent message."
        ),
        SimScenario(
            id = 3,
            setting = "CAFÉ NEAR CAMPUS",
            situation = "You need to check your bank balance. You see two open networks: " +
                    "\"CafeWiFi\" and \"CafeWiFi_FREE\". Neither asks for a password.",
            question = "How do you check your balance?",
            choices = listOf(
                SimChoice(
                    "Connect to the stronger signal and log in",
                    isSafe = false,
                    consequence = "One of those networks was a laptop under the next table. Your " +
                            "session was intercepted in transit."
                ),
                SimChoice(
                    "Use your mobile data instead",
                    isSafe = true,
                    consequence = "Correct. Mobile data costs a little, but no stranger sits " +
                            "between you and your bank."
                ),
                SimChoice(
                    "Connect, but only browse social media",
                    isSafe = false,
                    consequence = "Social sessions get hijacked too — and your email is usually the " +
                            "reset path for every other account you own."
                )
            ),
            conceptName = "Evil Twin & Man-in-the-Middle",
            conceptExplanation = "Anyone can broadcast a network with a trustworthy-looking name. " +
                    "On an untrusted network, assume someone is reading the traffic."
        ),
        SimScenario(
            id = 4,
            setting = "GROUP CHAT",
            situation = "A classmate messages you: \"Pre, pahiram ng portal password mo, I'll " +
                    "submit our group requirement for you. Deadline na bukas.\"",
            question = "How do you respond?",
            choices = listOf(
                SimChoice(
                    "Send it — you trust them, it's just a requirement",
                    isSafe = false,
                    consequence = "Their account was already compromised. The message wasn't from " +
                            "your classmate at all, and now your portal is exposed."
                ),
                SimChoice(
                    "Decline and submit the requirement yourself",
                    isSafe = true,
                    consequence = "Correct. Credentials are never shared, even with people you " +
                            "trust — and especially not under deadline pressure."
                ),
                SimChoice(
                    "Share it, then change your password afterwards",
                    isSafe = false,
                    consequence = "Too late. The account can be accessed, and recovery details " +
                            "changed, long before you get around to resetting it."
                )
            ),
            conceptName = "Social Engineering via Trust",
            conceptExplanation = "The most effective attacks come from someone you know — because " +
                    "their account was taken over first. Verify through another channel."
        ),
        SimScenario(
            id = 5,
            setting = "YOUR PHONE",
            situation = "While reading an article, a full-screen popup appears: \"⚠ YOUR DEVICE IS " +
                    "INFECTED! 3 viruses detected. Install CleanerPro NOW to remove them.\"",
            question = "What do you do?",
            choices = listOf(
                SimChoice(
                    "Install the app it recommends",
                    isSafe = false,
                    consequence = "There were no viruses — until now. You installed the malware the " +
                            "popup was advertising."
                ),
                SimChoice(
                    "Close the tab and run your real security app if worried",
                    isSafe = true,
                    consequence = "Correct. A web page can't scan your device. Only software " +
                            "already installed can tell you anything real."
                ),
                SimChoice(
                    "Tap the popup to read the scan details first",
                    isSafe = false,
                    consequence = "The whole overlay was one big button. Tapping anywhere inside it " +
                            "triggered the download."
                )
            ),
            conceptName = "Scareware",
            conceptExplanation = "Fake alerts manufacture panic to make you install something " +
                    "harmful. A website has no ability to scan your phone for viruses."
        )
    )
)

// ===========================================================================
// 3. PALETTE (file-scoped)
// ===========================================================================

private val SimSuccess: Color get() = AppSuccess
private val SimDanger: Color get() = AppDanger

// ===========================================================================
// 4. SIMULATION SCREEN
// ===========================================================================

/**
 * A scenario-based decision quiz. Runs a [ScenarioQuiz] end to end: briefing → scenarios → results.
 * Stateless with respect to the rest of the app — it reports the XP earned
 * back through [onComplete] and lets the caller decide what that means.
 */
@Composable
fun ScenarioQuizScreen(
quiz: ScenarioQuiz,
xpReward: Int,
onExit: () -> Unit,
onComplete: (xpEarned: Int, correct: Int, total: Int) -> Unit,
onMistake: () -> Unit,
hearts: Int,
xpBalance: Int,
modifier: Modifier = Modifier,
/** True when this level was already completed — XP was paid out on the
 *  first clear, so nothing here should look like it's still up for grabs. */
isReplay: Boolean = false
) {

    var stage by remember { mutableStateOf(SimStage.BRIEFING) }
    var stepIndex by remember { mutableIntStateOf(0) }
    // Tapping an option only selects it; SUBMIT ANSWER commits it, so a
    // misclick can still be changed before it counts.
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chosenIndex by remember { mutableStateOf<Int?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var heartsLost by remember { mutableIntStateOf(0) }


    val total = quiz.scenarios.size

    Box(modifier = modifier.fillMaxSize().background(AppNavy)) {
        when (stage) {
            SimStage.BRIEFING -> BriefingStage(
                quiz = quiz,
                xpReward = xpReward,
                isReplay = isReplay,
                onExit = onExit,
                onBegin = { stage = SimStage.RUNNING }
            )

            SimStage.RUNNING -> ScenarioStage(
                scenario = quiz.scenarios[stepIndex],
                stepIndex = stepIndex,
                total = total,
                hearts = hearts,
                liveXp = scoreLevelXp(true, heartsLost, 0, true),
                xpBalance = xpBalance,
                isReplay = isReplay,
                selectedIndex = selectedIndex,
                chosenIndex = chosenIndex,
                onSelect = { index ->
                    if (chosenIndex == null) selectedIndex = index
                },
                onSubmit = {
                    val index = selectedIndex
                    if (chosenIndex == null && index != null) {
                        chosenIndex = index
                        if (quiz.scenarios[stepIndex].choices[index].isSafe) {
                            correctCount++
                        } else {
                            heartsLost++
                            onMistake()
                        }
                    }
                },
                onContinue = {
                    if (stepIndex == total - 1) {
                        stage = SimStage.RESULT
                    } else {
                        stepIndex++
                        selectedIndex = null
                        chosenIndex = null
                    }
                },
                onExit = onExit
            )

            SimStage.RESULT -> {
                val award = awardFor(
                    completed = true,
                    heartsLost = heartsLost,
                    hintsUsed = 0,
                    allCluesFound = true
                )
                ResultStage(
                    correct = correctCount,
                    total = total,
                    heartsLost = heartsLost,
                    award = award,
                    isReplay = isReplay,
                    onFinish = { onComplete(award.total, correctCount, total) }
                )
            }
        }
    }
}

private enum class SimStage { BRIEFING, RUNNING, RESULT }

// ===========================================================================
// 5. STAGES
// ===========================================================================

@Composable
private fun BriefingStage(
    quiz: ScenarioQuiz,
    xpReward: Int,
    isReplay: Boolean,
    onExit: () -> Unit,
    onBegin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onExit) {
            Icon(Icons.Filled.Close, contentDescription = "Exit quiz", tint = AppGray)
        }

        // Starts at the top and scrolls when it overflows — briefings vary in
        // length and small screens shouldn't push the start button off.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(84.dp).background(AppBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = AppOnBlue,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text("SCENARIO QUIZ", color = AppCyan, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text(quiz.title, color = AppWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(quiz.briefing, color = AppGray, fontSize = 15.sp, lineHeight = 22.sp)

            Spacer(Modifier.height(20.dp))

            Row {
                SimChip("${quiz.scenarios.size} scenarios")
                Spacer(Modifier.width(10.dp))
                if (isReplay) {
                    SimChip("Review · already completed")
                } else {
                    SimChip("up to +$xpReward XP")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onBegin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
        ) {
            Text("BEGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ScenarioStage(
    scenario: SimScenario,
    stepIndex: Int,
    total: Int,
    hearts: Int,
    liveXp: Int,
    xpBalance: Int,
    isReplay: Boolean,
    selectedIndex: Int?,
    chosenIndex: Int?,
    onSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit,
    onExit: () -> Unit
) {
    val answered = chosenIndex != null
    val progress by animateFloatAsState(
        targetValue = (stepIndex + if (answered) 1f else 0f) / total,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "simProgress"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // Progress header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit quiz", tint = AppGray)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(8.dp),
                color = AppCyan,
                trackColor = AppCard
            )
            Spacer(Modifier.width(12.dp))
            HeartsRow(hearts = hearts, heartSize = 14.dp)
            Spacer(Modifier.width(10.dp))
            XpIndicator(xp = xpBalance, iconSize = 14.dp, fontSize = 12.sp, showDelta = false)
            if (!isReplay) {
                Spacer(Modifier.width(4.dp))
                Text("+$liveXp", color = AppCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text("${stepIndex + 1}/$total", color = AppGray, fontSize = 13.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Situation card. Same frame the simulations use for anything the
            // student has to judge: a rail down the left edge and a corner
            // badge naming the setting. The rail says what kind of thing this
            // is, never whether it is safe.
            val frameShape = RoundedCornerShape(3.dp, 16.dp, 16.dp, 3.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(frameShape)
                    .background(AppCard)
                    .border(1.dp, AppCyan.copy(alpha = 0.30f), frameShape)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(AppCyan)
                )
                Column {
                    Box(
                        modifier = Modifier
                            .background(AppCyan, RoundedCornerShape(0.dp, 0.dp, 11.dp, 0.dp))
                            .padding(horizontal = 11.dp, vertical = 3.dp)
                    ) {
                        Text(scenario.setting, color = AppNavy, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Text(
                        scenario.situation,
                        color = AppWhite,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(
                            start = 14.dp, end = 16.dp, top = 12.dp, bottom = 16.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(scenario.question, color = AppWhite, fontSize = 15.sp,
                fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            scenario.choices.forEachIndexed { index, choice ->
                ChoiceRow(
                    choice = choice,
                    answered = answered,
                    isSelected = selectedIndex == index,
                    isChosen = chosenIndex == index,
                    onClick = { onSelect(index) }
                )
                Spacer(Modifier.height(10.dp))
            }

            // Feedback appears only after committing to a choice
            AnimatedVisibility(
                visible = answered,
                enter = fadeIn(tween(250)) + expandVertically(tween(250))
            ) {
                chosenIndex?.let { idx ->
                    FeedbackPanel(
                        choice = scenario.choices[idx],
                        conceptName = scenario.conceptName,
                        conceptExplanation = scenario.conceptExplanation
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // Submit / continue bar
        Box(modifier = Modifier.fillMaxWidth().background(AppCard).padding(16.dp)) {
            Button(
                onClick = if (answered) onContinue else onSubmit,
                enabled = answered || selectedIndex != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppCyan,
                    contentColor = AppNavy,
                    disabledContainerColor = AppNavy,
                    disabledContentColor = AppGray
                )
            ) {
                Text(
                    text = when {
                        !answered -> "SUBMIT ANSWER"
                        stepIndex == total - 1 -> "SEE RESULTS"
                        else -> "CONTINUE"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    choice: SimChoice,
    answered: Boolean,
    isSelected: Boolean,
    isChosen: Boolean,
    onClick: () -> Unit
) {
    // Before answering only the pending selection is highlighted. After
    // answering, the safe option is always revealed — not just the one the
    // user picked.
    val borderColor = when {
        !answered && isSelected -> AppCyan
        !answered -> AppGray.copy(alpha = 0.3f)
        choice.isSafe -> SimSuccess
        isChosen -> SimDanger
        else -> AppGray.copy(alpha = 0.15f)
    }

    val textColor = when {
        !answered -> AppWhite
        choice.isSafe -> AppWhite
        isChosen -> AppWhite
        else -> AppGray.copy(alpha = 0.55f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    answered && choice.isSafe -> SimSuccess.copy(alpha = 0.10f)
                    !answered && isSelected -> AppCyan.copy(alpha = 0.14f)
                    else -> AppCard
                },
                shape = RoundedCornerShape(14.dp)
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !answered, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = choice.text,
            color = textColor,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        if (answered && (choice.isSafe || isChosen)) {
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = if (choice.isSafe) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = if (choice.isSafe) "Safe choice" else "Risky choice",
                tint = if (choice.isSafe) SimSuccess else SimDanger,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FeedbackPanel(
    choice: SimChoice,
    conceptName: String,
    conceptExplanation: String
) {
    val accent = if (choice.isSafe) SimSuccess else SimDanger

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (choice.isSafe) Icons.Filled.Check else Icons.Filled.Warning,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (choice.isSafe) "Good call" else "That went badly",
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(choice.consequence, color = AppWhite, fontSize = 14.sp, lineHeight = 21.sp)

        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = 0.2f)))
        Spacer(Modifier.height(14.dp))

        Text(conceptName.uppercase(), color = AppCyan, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        Text(conceptExplanation, color = AppGray, fontSize = 13.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun ResultStage(
    correct: Int,
    total: Int,
    heartsLost: Int,
    award: XpAward,
    isReplay: Boolean,
    onFinish: () -> Unit
) {
    val passed = correct >= (total + 1) / 2  // majority correct
    val accent = if (passed) SimSuccess else SimDanger

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(96.dp).background(accent.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (passed) Icons.Filled.Check else Icons.Filled.Warning,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (passed) "Quiz cleared" else "Quiz complete",
            color = AppWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (passed) {
                "You handled $correct of $total scenarios safely."
            } else {
                "You handled $correct of $total safely — replay to sharpen the rest."
            },
            color = AppGray,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(28.dp))

        if (isReplay) {
            ReplayNotice()
        } else {
            XpBreakdown(
                award = award,
                heartsLost = heartsLost,
                hintsUsed = 0,
                hintsPaid = 0,
                quiz = true
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
        ) {
            Text("CONTINUE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun SimChip(text: String) {
    Box(
        modifier = Modifier
            .background(AppCard, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, color = AppWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
