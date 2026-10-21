package com.cyberity.cvsu

import android.app.Activity
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

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
// 2. LEVEL 105 CONTENT — "Fundamentals Quiz"
// ===========================================================================
// One scenario per Unit 1 idea: alert triage (102), the CIA triad (103),
// least privilege and fail-safe (104), and likelihood x impact (106). Every
// wrong option is a mistake people really make, the options are the same
// length and shape, and the safe choice moves between positions.

fun spotTheThreatQuiz(): ScenarioQuiz = ScenarioQuiz(
    levelId = 105,
    title = "Fundamentals Quiz",
    briefing = "Five situations from a week at the CvSU IT Services Office, one for each idea " +
            "in this unit: alerts, the CIA triad, least privilege, failing safe and risk. " +
            "Choose what you'd actually do — you'll see the consequence either way.",
    scenarios = listOf(
        SimScenario(
            id = 1,
            setting = "ITSO NIGHT SHIFT",
            situation = "2:04 AM. A HIGH alert fires: \"Large outbound transfer: 38 GB from " +
                    "records-db to an external address.\" The asset notes say records-db is " +
                    "backed up every night at 2:00 AM to the university's contracted cloud " +
                    "backup provider. Last night's transfer was 37 GB, to the same address.",
            question = "What's the right call?",
            choices = listOf(
                SimChoice(
                    "Check it matches the backup schedule and address, then close it as a " +
                            "false positive",
                    isSafe = true,
                    consequence = "Correct. The time, the size and the destination all match a " +
                            "documented nightly job. You proved it with evidence, closed it, and " +
                            "kept the rule watching for the night it doesn't match."
                ),
                SimChoice(
                    "Escalate it as a data breach, since it's HIGH and leaving the network at night",
                    isSafe = false,
                    consequence = "The incident team spent the morning on the backup job, and the " +
                            "real alerts from that night waited. Severity is the tool's guess, " +
                            "not a verdict."
                ),
                SimChoice(
                    "Switch the alert rule off, because it fires every night and wastes the shift",
                    isSafe = false,
                    consequence = "Now nothing watches records-db at all. The night someone copies " +
                            "it to their own server at 2:00 AM, there won't be an alert to ignore."
                )
            ),
            conceptName = "False Positives",
            conceptExplanation = "An alert that fires on real but legitimate activity is a false " +
                    "positive. Prove it with something that explains it, like a schedule, a " +
                    "documented process or the host's role, then close it. Don't escalate by " +
                    "severity, and don't silence the rule."
        ),
        SimScenario(
            id = 2,
            setting = "REGISTRAR'S OFFICE",
            situation = "A student's final grade changed from 2.75 to 1.25 two weeks after the " +
                    "instructor submitted it. The instructor didn't change it. The records " +
                    "system was online all week, and the file was never shared outside the " +
                    "office.",
            question = "Which part of the CIA triad was broken?",
            choices = listOf(
                SimChoice(
                    "Confidentiality: someone reached a record they had no right to see",
                    isSafe = false,
                    consequence = "Close, but the report rules it out: the file never left the " +
                            "office. What's wrong is that the grade itself can't be trusted " +
                            "anymore."
                ),
                SimChoice(
                    "Availability: the correct grade wasn't there when the student needed it",
                    isSafe = false,
                    consequence = "The system was online all week and the record opened fine. " +
                            "Nothing was out of reach. The record was wrong."
                ),
                SimChoice(
                    "Integrity: the record was changed by someone who wasn't allowed to change it",
                    isSafe = true,
                    consequence = "Correct. The data was altered without authorisation. The fix " +
                            "is verification, like hashes and an audit trail, not tighter sharing " +
                            "or more uptime."
                )
            ),
            conceptName = "The CIA Triad",
            conceptExplanation = "Ask of any incident: was the data seen, changed, or unreachable? " +
                    "Seen by the wrong people is confidentiality, changed without permission is " +
                    "integrity, out of reach is availability. Each points to a different fix."
        ),
        SimScenario(
            id = 3,
            setting = "COMPUTER LAB 2",
            situation = "Your new student assistant will reset lab PCs between classes and log " +
                    "broken equipment. On day one they ask for the same administrator account " +
                    "you use, \"so I won't have to bother you every time.\"",
            question = "How do you set up their access?",
            choices = listOf(
                SimChoice(
                    "Give them the admin account, and ask them to use it only for lab work",
                    isSafe = false,
                    consequence = "A month later their laptop catches an infostealer. The " +
                            "attacker now has an account that can change every system you " +
                            "manage, not just Lab 2."
                ),
                SimChoice(
                    "Make them their own account that can only reset lab PCs and log issues",
                    isSafe = true,
                    consequence = "Correct. They can do the whole job, and if that account is " +
                            "ever stolen, the damage stops at resetting lab PCs."
                ),
                SimChoice(
                    "Share your admin password for this week, then change it on Friday",
                    isSafe = false,
                    consequence = "For a week, every action they take is logged under your name, " +
                            "and the password can be copied long before Friday comes."
                )
            ),
            conceptName = "Least Privilege",
            conceptExplanation = "Give each person the smallest access that still lets them do " +
                    "the work, and nothing beyond it. Access you never gave can't be misused or " +
                    "stolen."
        ),
        SimScenario(
            id = 4,
            setting = "SERVER ROOM",
            situation = "During a brownout, the electronic lock on the server room door clicked " +
                    "open and stayed open for 40 minutes until power came back. Facilities asks " +
                    "how the lock should behave the next time the power fails.",
            question = "What do you tell them?",
            choices = listOf(
                SimChoice(
                    "Stay unlocked, so ITSO can get in quickly to check on the servers",
                    isSafe = false,
                    consequence = "That's exactly what happened during the brownout: 40 minutes " +
                            "with the most valuable room on campus open to anyone walking past."
                ),
                SimChoice(
                    "Stay unlocked, but turn CCTV recording on until the power comes back",
                    isSafe = false,
                    consequence = "The camera records someone walking out with a server. It " +
                            "doesn't stop them. Watching an open door isn't the same as closing it."
                ),
                SimChoice(
                    "Stay locked from outside, with a key for ITSO and a free exit from inside",
                    isSafe = true,
                    consequence = "Correct. When the power fails, the door fails into its secure " +
                            "state. ITSO still gets in with a key, and nobody is trapped inside."
                )
            ),
            conceptName = "Fail-Safe",
            conceptExplanation = "Every control fails sometimes. Decide in advance which way it " +
                    "fails, and make it fail into the secure state, not the open one. Plan a " +
                    "safe way for authorised people to get through when it does."
        ),
        SimScenario(
            id = 5,
            setting = "ITSO BUDGET MEETING",
            situation = "There's money for one fix this month. Risk A: lab mice go missing almost " +
                    "every week, about ₱300 each. Risk B: the enrollment database has no offsite " +
                    "backup. A fire or ransomware is unlikely in any given year, but either would " +
                    "wipe every student record.",
            question = "Which risk do you fund first?",
            choices = listOf(
                SimChoice(
                    "Offsite backups for enrollment: rare, but the damage would be catastrophic",
                    isSafe = true,
                    consequence = "Correct. Low likelihood times catastrophic impact still " +
                            "outranks frequent but cheap losses. The mice can wait a month; " +
                            "18,000 student records can't be bought back."
                ),
                SimChoice(
                    "Locks for the lab mice: they go missing every week, so they're more likely",
                    isSafe = false,
                    consequence = "You saved about ₱1,200 this month. The enrollment database is " +
                            "still one bad night away from being gone for good."
                ),
                SimChoice(
                    "Split the money evenly, so both risks get at least some protection",
                    isSafe = false,
                    consequence = "Half a backup isn't a backup, and half the mouse locks still " +
                            "lose mice. Spreading money thin leaves both risks open."
                )
            ),
            conceptName = "Likelihood × Impact",
            conceptExplanation = "A risk's size is how likely it is times how bad it would be. " +
                    "Rank by that, not by which one happens most often or which one you hear " +
                    "about most."
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
            QuizSettingsButton()
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

/** Same gear and dialog as the lab header, so every level offers the same settings. */
@Composable
private fun QuizSettingsButton() {
    var showSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(38.dp)
            .background(AppCard, RoundedCornerShape(10.dp))
            .clickable { showSettings = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = AppCyan,
            modifier = Modifier.size(20.dp)
        )
    }

    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false },
            onSignOut = {
                showSettings = false
                FirebaseAuth.getInstance().signOut()
                (context as? Activity)?.recreate()
            }
        )
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
