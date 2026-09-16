package com.cyberity.cvsu

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

// ===========================================================================
// 1. DATA MODELS
// ===========================================================================

enum class LevelStatus { LOCKED, CURRENT, COMPLETED }

enum class LevelType { LESSON, QUIZ, SIMULATION, CHALLENGE, REWARD }

@Immutable
data class LearningLevel(
    val id: Int,
    val title: String,
    val description: String,
    val xpReward: Int,
    val type: LevelType,
    val status: LevelStatus,
    /** Shown in the bottom sheet. Simulations/challenges tend to run longer. */
    val durationMinutes: Int = 5
)

@Immutable
data class LearningUnit(
    val id: Int,
    val title: String,
    val description: String,
    val levels: List<LearningLevel>
) {
    /** REWARD nodes are bonuses, not curriculum — they don't count toward "x / y levels". */
    val gradedLevels: List<LearningLevel> get() = levels.filter { it.type != LevelType.REWARD }
    val completedCount: Int get() = gradedLevels.count { it.status == LevelStatus.COMPLETED }
    val totalCount: Int get() = gradedLevels.size
    val isLocked: Boolean get() = levels.none { it.status != LevelStatus.LOCKED }
    val isComplete: Boolean get() = completedCount == totalCount && totalCount > 0
}

// ===========================================================================
// 2. CURRICULUM
// ===========================================================================

fun sampleLearningUnits(): List<LearningUnit> = listOf(
    LearningUnit(
        id = 1,
        title = "Cybersecurity Fundamentals",
        description = "Start here — the language and mindset of security",
        levels = listOf(
            LearningLevel(101, "Inbox Triage", "Security lab: investigate a live mailbox, follow the phishing link in a sandboxed browser, capture the flag.", 50, LevelType.SIMULATION, LevelStatus.CURRENT, durationMinutes = 12),
            LearningLevel(102, "Cybersecurity Threats", "Meet the main categories of threat you'll defend against.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(103, "CIA Triad", "Confidentiality, Integrity, Availability — the core model.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(150, "Bonus XP Cache", "A quick reward for clearing the first three levels.", 50, LevelType.REWARD, LevelStatus.LOCKED, durationMinutes = 1),
            LearningLevel(104, "Security Principles", "Least privilege, defence in depth, and fail-safe defaults.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(105, "Fundamentals Quiz", "Prove you've got the basics locked down.", 40, LevelType.QUIZ, LevelStatus.LOCKED, durationMinutes = 8)
        )
    ),
    LearningUnit(
        id = 2,
        title = "Password & Account Security",
        description = "Credentials are the front door — learn to lock it",
        levels = listOf(
            LearningLevel(201, "Strong Passwords", "What actually makes a password hard to crack.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(202, "Password Attacks", "Brute force, dictionary attacks, and credential stuffing.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(203, "Multi-Factor Authentication", "Why a second factor defeats most credential theft.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(250, "Cyber Challenge", "Crack a weak password set against the clock.", 60, LevelType.CHALLENGE, LevelStatus.LOCKED, durationMinutes = 10),
            LearningLevel(204, "Account Protection", "Recovery options, session hygiene, and breach response.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(205, "Password Security Challenge", "Audit a set of real-world account configurations.", 50, LevelType.SIMULATION, LevelStatus.LOCKED, durationMinutes = 12)
        )
    ),
    LearningUnit(
        id = 3,
        title = "Phishing & Social Engineering",
        description = "The attacks that target people, not systems",
        levels = listOf(
            LearningLevel(301, "What is Phishing?", "How deceptive messages bypass technical controls.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(302, "Identifying Suspicious Emails", "Headers, domains, and the tells that give it away.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(303, "Social Engineering", "Pretexting, baiting, and manufactured urgency.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(304, "Scam Messages", "Smishing and vishing in the Philippine context.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(350, "Quick Quiz", "Six questions, sixty seconds.", 30, LevelType.QUIZ, LevelStatus.LOCKED, durationMinutes = 3),
            LearningLevel(305, "Phishing Simulation", "Inspect a live inbox and flag every malicious message.", 60, LevelType.SIMULATION, LevelStatus.LOCKED, durationMinutes = 15)
        )
    ),
    LearningUnit(
        id = 4,
        title = "Malware",
        description = "Recognise hostile code and stop it spreading",
        levels = listOf(
            LearningLevel(401, "What is Malware?", "Classify hostile software by how it behaves.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(402, "Viruses & Trojans", "Replication, payloads, and disguised installers.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(403, "Ransomware", "Encryption extortion and why backups matter most.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(404, "Malware Prevention", "Patching, allowlisting, and endpoint hardening.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(450, "Bonus XP Cache", "Reward for surviving the malware unit.", 50, LevelType.REWARD, LevelStatus.LOCKED, durationMinutes = 1),
            LearningLevel(405, "Malware Identification Simulation", "Triage suspicious processes on a compromised host.", 60, LevelType.SIMULATION, LevelStatus.LOCKED, durationMinutes = 15)
        )
    ),
    LearningUnit(
        id = 5,
        title = "Network Security",
        description = "Defend the traffic moving between machines",
        levels = listOf(
            LearningLevel(501, "Network Basics", "Packets, ports, and how traffic actually flows.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(502, "Wi-Fi Security", "WPA generations and the risk of open networks.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(503, "Safe Browsing", "TLS, certificate warnings, and hostile networks.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(504, "Network Threats", "Sniffing, spoofing, and man-in-the-middle attacks.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(550, "Cyber Challenge", "Spot the rogue access point in a campus network.", 60, LevelType.CHALLENGE, LevelStatus.LOCKED, durationMinutes = 10),
            LearningLevel(505, "Network Security Simulation", "Configure a firewall against live hostile traffic.", 60, LevelType.SIMULATION, LevelStatus.LOCKED, durationMinutes = 15)
        )
    ),
    LearningUnit(
        id = 6,
        title = "Incident Response",
        description = "What to do when defences have already failed",
        levels = listOf(
            LearningLevel(601, "Security Incidents", "What counts as an incident, and who to tell.", 20, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(602, "Detection", "Logs, alerts, and separating signal from noise.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(603, "Containment", "Isolate the damage before you start cleaning up.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(604, "Recovery", "Restore safely and close the hole behind you.", 25, LevelType.LESSON, LevelStatus.LOCKED),
            LearningLevel(605, "Incident Response Simulation", "Run point on a live breach from alert to write-up.", 80, LevelType.SIMULATION, LevelStatus.LOCKED, durationMinutes = 20)
        )
    )
)

/**
 * Marks a level complete and promotes the next LOCKED level to CURRENT.
 * Pure function over the list — no mutable holder, so there's no property/setter
 * collision risk and the result is trivially testable.
 */
fun List<LearningUnit>.withLevelCompleted(levelId: Int): List<LearningUnit> {
    val flat = flatMap { it.levels }
    val index = flat.indexOfFirst { it.id == levelId }
    if (index < 0) return this

    val nextId = flat.drop(index + 1).firstOrNull { it.status == LevelStatus.LOCKED }?.id

    return map { unit ->
        unit.copy(
            levels = unit.levels.map { level ->
                when (level.id) {
                    levelId -> level.copy(status = LevelStatus.COMPLETED)
                    nextId -> level.copy(status = LevelStatus.CURRENT)
                    else -> level
                }
            }
        )
    }
}

// ===========================================================================
// 3. FLATTENED ROW MODEL
// ===========================================================================
// A LazyColumn recycles, so no single composable can draw a path spanning
// several items. Instead we flatten units+levels into one row list, and give
// each row exactly the neighbour info it needs to draw its own half-segments.

private sealed interface PathRow {

    data class UnitBanner(
        val unit: LearningUnit,
        val incomingStatus: LevelStatus?,
        val incomingIndex: Int,
        val outgoingIndex: Int
    ) : PathRow

    data class Level(
        val level: LearningLevel,
        val unit: LearningUnit,
        val globalIndex: Int,
        val prevLevel: LearningLevel?,
        val prevIndex: Int,
        val hasNext: Boolean,
        val nextIndex: Int
    ) : PathRow
}

private fun buildPathRows(units: List<LearningUnit>): List<PathRow> {
    val rows = mutableListOf<PathRow>()
    var globalIndex = 0
    var prevLevel: LearningLevel? = null
    var prevIndex = -1

    units.forEachIndexed { unitPos, unit ->
        rows += PathRow.UnitBanner(
            unit = unit,
            incomingStatus = prevLevel?.status,
            incomingIndex = prevIndex,
            outgoingIndex = globalIndex
        )

        unit.levels.forEachIndexed { levelPos, level ->
            val isLastOverall = unitPos == units.lastIndex && levelPos == unit.levels.lastIndex
            rows += PathRow.Level(
                level = level,
                unit = unit,
                globalIndex = globalIndex,
                prevLevel = prevLevel,
                prevIndex = prevIndex,
                hasNext = !isLastOverall,
                nextIndex = globalIndex + 1
            )
            prevLevel = level
            prevIndex = globalIndex
            globalIndex++
        }
    }
    return rows
}

// ===========================================================================
// 4. PATH GEOMETRY
// ===========================================================================

/**
 * Horizontal offset factor as a sine of the node index. An 8-step period gives
 * 0 → .71 → 1 → .71 → 0 → -.71 → -1 → -.71, so nodes drift rather than
 * snapping between three fixed columns — closer to a natural weave, and it
 * extends to unlimited nodes with no lookup table.
 */
private fun horizontalFactor(globalIndex: Int): Float {
    if (globalIndex < 0) return 0f
    return sin(globalIndex * PI / 4.0).toFloat()
}

/** Swing as a fraction of row width — shared by drawing AND layout so they can't drift. */
private const val AMPLITUDE_RATIO = 0.24f

private val RowHeight: Dp = 132.dp
private val NodeSizeNormal: Dp = 64.dp
private val NodeSizeCurrent: Dp = 78.dp
private val NodeSizeReward: Dp = 58.dp
private val TrailWidth: Dp = 11.dp

private val AccentSuccess = Color(0xFF27E0A8)
private val AccentReward = Color(0xFF7FD4FF)

private fun nodeSizeFor(level: LearningLevel): Dp = when {
    level.status == LevelStatus.CURRENT -> NodeSizeCurrent
    level.type == LevelType.REWARD -> NodeSizeReward
    else -> NodeSizeNormal
}

private fun iconFor(type: LevelType): ImageVector = when (type) {
    LevelType.LESSON -> Icons.Filled.List
    LevelType.QUIZ -> Icons.Filled.Edit
    LevelType.SIMULATION -> Icons.Filled.Warning
    LevelType.CHALLENGE -> Icons.Filled.Build
    LevelType.REWARD -> Icons.Filled.Star
}

private fun typeLabel(type: LevelType): String = when (type) {
    LevelType.LESSON -> "LESSON"
    LevelType.QUIZ -> "QUIZ"
    LevelType.SIMULATION -> "SIMULATION"
    LevelType.CHALLENGE -> "CHALLENGE"
    LevelType.REWARD -> "BONUS"
}

// ===========================================================================
// 5. LEARN SCREEN
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    onStartLevel: (LearningLevel) -> Unit = {},
    onLevelRunningChanged: (Boolean) -> Unit = {}
) {
    var units by remember { mutableStateOf(sampleLearningUnits()) }
    var selected by remember { mutableStateOf<Pair<LearningUnit, LearningLevel>?>(null) }
    // When non-null, the simulation takes over the whole Learn area.
    var runningLevel by remember { mutableStateOf<LearningLevel?>(null) }

    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(runningLevel) {
        onLevelRunningChanged(runningLevel != null)
    }

    val totalXp = remember(units) {
        units.flatMap { it.levels }
            .filter { it.status == LevelStatus.COMPLETED }
            .sumOf { it.xpReward }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val running = runningLevel
    if (running != null) {
        val requestExit = { showExitConfirm = true }

        // Hardware/gesture back always asks for confirmation too. LabScreen has its
        // own BackHandler for stepping back inside its WebView first — that one takes
        // priority while active; this one is the fallback for every other level type
        // and for LabScreen whenever its own handler is disabled.
        BackHandler(enabled = true) { requestExit() }

        // A running level takes over the whole Learn area.
        when (val content = contentFor(running.id)) {
            is LevelContent.Inbox -> InboxSimulationScreen(
                simulation = content.simulation,
                xpReward = running.xpReward,
                modifier = modifier,
                onExit = requestExit,
                onComplete = { _, _, _ ->
                    units = units.withLevelCompleted(running.id)
                    runningLevel = null
                }
            )

            is LevelContent.Scenarios -> ScenarioQuizScreen(
                quiz = content.quiz,
                xpReward = running.xpReward,
                modifier = modifier,
                onExit = requestExit,
                onComplete = { _, _, _ ->
                    units = units.withLevelCompleted(running.id)
                    runningLevel = null
                }
            )

            is LevelContent.Lab -> LabScreen(
                lab = content.lab,
                xpReward = running.xpReward,
                modifier = modifier,
                clueLabels = content.clueLabels,
                onExit = requestExit,
                onComplete = { _, _, _ ->
                    units = units.withLevelCompleted(running.id)
                    runningLevel = null
                }
            )

            null -> ComingSoonLevel(
                level = running,
                modifier = modifier,
                onBack = { runningLevel = null }
            )
        }

        if (showExitConfirm) {
            ExitLevelDialog(
                onDismiss = { showExitConfirm = false },
                onConfirm = {
                    showExitConfirm = false
                    runningLevel = null
                }
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize().background(AppNavy)) {
            LearnHeader(streak = 0, xp = totalXp, energy = 25)

            LearningPath(
                units = units,
                modifier = Modifier.weight(1f),
                onLevelClick = { unit, level -> selected = unit to level }
            )
        }

        selected?.let { (unit, level) ->
            LevelPreviewBottomSheet(
                unit = unit,
                level = level,
                prerequisiteTitle = prerequisiteFor(units, level),
                sheetState = sheetState,
                onDismiss = { selected = null },
                onStart = {
                    selected = null
                    runningLevel = level
                    onStartLevel(level)
                }
            )
        }
    }
}

/** Guards against losing progress to an accidental back-press or tap. */
@Composable
private fun ExitLevelDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppCard,
        title = { Text("Exit this level?", color = AppWhite, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Your progress on this level won't be saved.",
                color = AppGray,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("EXIT", color = AccentReward, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("STAY", color = AppGray)
            }
        }
    )
}

/** Title of the level that must be finished before [level] unlocks. */
private fun prerequisiteFor(units: List<LearningUnit>, level: LearningLevel): String? {
    val flat = units.flatMap { it.levels }
    val index = flat.indexOfFirst { it.id == level.id }
    if (index <= 0) return null
    return flat.take(index).lastOrNull { it.status != LevelStatus.COMPLETED }?.title
}

// ===========================================================================
// 6. TOP STATUS BAR
// ===========================================================================

@Composable
fun LearnHeader(
    streak: Int,
    xp: Int,
    energy: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppCard)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatPill(Icons.Filled.DateRange, streak.toString(), AppCyan, "Day streak")
        StatPill(Icons.Filled.Star, xp.toString(), AppWhite, "Total XP")
        StatPill(Icons.Filled.Favorite, energy.toString(), AccentReward, "Energy")
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    value: String,
    tint: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(value, color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// ===========================================================================
// 7. THE PATH
// ===========================================================================

@Composable
fun LearningPath(
    units: List<LearningUnit>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 40.dp),
    onLevelClick: (LearningUnit, LearningLevel) -> Unit
) {
    val rows = remember(units) { buildPathRows(units) }
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(AppNavy),
        contentPadding = contentPadding
    ) {
        items(
            items = rows,
            key = { row ->
                when (row) {
                    is PathRow.UnitBanner -> "unit_${row.unit.id}"
                    is PathRow.Level -> "level_${row.level.id}"
                }
            }
        ) { row ->
            when (row) {
                is PathRow.UnitBanner -> UnitHeader(row)
                is PathRow.Level -> LevelRow(
                    row = row,
                    onClick = { onLevelClick(row.unit, row.level) }
                )
            }
        }
    }
}

// ===========================================================================
// 8. UNIT HEADER — also carries the trail through its own height
// ===========================================================================

/**
 * Row wrapper: draws the connector passing behind the banner, then lays the
 * banner card on top of it. Private because it takes the internal row type.
 */
@Composable
private fun UnitHeader(row: PathRow.UnitBanner) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .drawBehind {
                if (row.incomingStatus == null) return@drawBehind

                val amplitude = size.width * AMPLITUDE_RATIO
                val centerX = size.width / 2f
                val fromX = centerX + amplitude * horizontalFactor(row.incomingIndex)
                val toX = centerX + amplitude * horizontalFactor(row.outgoingIndex)
                // Both neighbouring level rows terminate their halves at this same
                // midpoint x, so one straight stroke spanning the banner's height
                // reads as the path continuing underneath it.
                val seamX = (fromX + toX) / 2f

                drawPathConnector(
                    start = Offset(seamX, 0f),
                    end = Offset(seamX, size.height),
                    strokeWidth = with(density) { TrailWidth.toPx() },
                    lit = row.incomingStatus == LevelStatus.COMPLETED,
                    straight = true
                )
            }
    ) {
        UnitBanner(unit = row.unit)
    }
}

/** The unit card itself — reusable anywhere you need to show a unit summary. */
@Composable
fun UnitBanner(
    unit: LearningUnit,
    modifier: Modifier = Modifier
) {
    val locked = unit.isLocked

    val containerBrush = if (locked) {
        Brush.horizontalGradient(listOf(AppCard, AppCard))
    } else {
        Brush.horizontalGradient(listOf(AppBlue, AppBlue.copy(alpha = 0.72f)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(brush = containerBrush, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "UNIT ${unit.id}",
                    color = if (locked) AppGray else AppCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = unit.title,
                    color = if (locked) AppGray else AppWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (locked) "Locked" else "${unit.completedCount} / ${unit.totalCount} levels completed",
                    color = if (locked) AppGray.copy(alpha = 0.7f) else AppWhite.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )

                if (!locked) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (unit.totalCount == 0) 0f
                            else unit.completedCount.toFloat() / unit.totalCount
                        },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = AppCyan,
                        trackColor = AppNavy.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (locked) AppNavy.copy(alpha = 0.5f) else AppNavy.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (locked) Icons.Filled.Lock else Icons.Filled.List,
                    contentDescription = if (locked) "Unit locked" else "Unit syllabus",
                    tint = if (locked) AppGray else AppWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ===========================================================================
// 9. LEVEL ROW — node plus the two trail halves that touch it
// ===========================================================================

@Composable
private fun LevelRow(
    row: PathRow.Level,
    onClick: () -> Unit
) {
    val level = row.level

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
    ) {
        NodeOffsetHost(globalIndex = row.globalIndex) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (level.type == LevelType.REWARD) {
                    RewardNode(level = level, onClick = onClick)
                } else {
                    LevelNode(level = level, onClick = onClick)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = level.title,
                    color = when (level.status) {
                        LevelStatus.CURRENT -> AppWhite
                        LevelStatus.COMPLETED -> AppWhite.copy(alpha = 0.78f)
                        LevelStatus.LOCKED -> AppGray
                    },
                    fontSize = 12.sp,
                    fontWeight = if (level.status == LevelStatus.CURRENT) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.widthIn(max = 130.dp).padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/** Positions content at the snake offset for [globalIndex], matching the drawn path exactly. */
@Composable
private fun NodeOffsetHost(
    globalIndex: Int,
    content: @Composable () -> Unit
) {
    var parentWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier.fillMaxWidth().onSizeChanged { parentWidthPx = it.width },
        contentAlignment = Alignment.TopCenter
    ) {
        val offsetDp = with(density) {
            (parentWidthPx * AMPLITUDE_RATIO * horizontalFactor(globalIndex)).toDp()
        }
        Box(modifier = Modifier.offset(x = offsetDp)) { content() }
    }
}

// ===========================================================================
// 10. PATH CONNECTOR
// ===========================================================================

/**
 * Draws one half-connector. This is a DrawScope extension rather than a
 * composable: a composable connector would be its own LazyColumn item and
 * couldn't align with nodes across recycling boundaries.
 *
 * Control points sit directly above/below the endpoints, so each curve leaves
 * and enters vertically and the two halves meet tangentially at the row seam.
 */
private fun DrawScope.drawPathConnector(
    start: Offset,
    end: Offset,
    strokeWidth: Float,
    lit: Boolean,
    straight: Boolean = false
) {
    val path = Path().apply {
        moveTo(start.x, start.y)
        if (straight) {
            lineTo(end.x, end.y)
        } else {
            val dy = end.y - start.y
            cubicTo(start.x, start.y + dy * 0.5f, end.x, end.y - dy * 0.5f, end.x, end.y)
        }
    }

    if (lit) {
        drawPath(path, AppCyan.copy(alpha = 0.16f), style = Stroke(strokeWidth * 2.2f, cap = StrokeCap.Round))
        drawPath(path, AppCyan.copy(alpha = 0.8f), style = Stroke(strokeWidth, cap = StrokeCap.Round))
    } else {
        drawPath(path, AppCard, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        drawPath(path, AppGray.copy(alpha = 0.16f), style = Stroke(strokeWidth * 0.4f, cap = StrokeCap.Round))
    }
}

// ===========================================================================
// 11. NODES
// ===========================================================================

/** Standard circular level node — lesson, quiz, simulation or challenge. */
@Composable
fun LevelNode(
    level: LearningLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val ringStrokePx = with(density) { 5.dp.toPx() }
    val size = nodeSizeFor(level)

    // Gentle pulse, current level only — draws the eye without being noisy.
    val pulse = if (level.status == LevelStatus.CURRENT) {
        val transition = rememberInfiniteTransition(label = "currentPulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        ).value
    } else 1f

    val fill = when (level.status) {
        LevelStatus.CURRENT -> AppCyan
        LevelStatus.COMPLETED -> AppBlue
        LevelStatus.LOCKED -> AppCard
    }

    Box(
        modifier = modifier.size(size + 14.dp).scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size + 14.dp)
                .drawBehind {
                    val r = (this.size.minDimension / 2f) - ringStrokePx / 2f
                    when (level.status) {
                        LevelStatus.CURRENT -> {
                            drawCircle(AppCyan.copy(alpha = 0.14f), radius = r + ringStrokePx * 1.8f)
                            drawCircle(AppCyan.copy(alpha = 0.30f), radius = r, style = Stroke(ringStrokePx))
                        }
                        LevelStatus.COMPLETED -> {
                            drawCircle(AppCyan.copy(alpha = 0.55f), radius = r, style = Stroke(ringStrokePx))
                        }
                        LevelStatus.LOCKED -> {
                            drawCircle(AppGray.copy(alpha = 0.14f), radius = r, style = Stroke(ringStrokePx * 0.6f))
                        }
                    }
                }
        )

        Box(
            modifier = Modifier
                .size(size)
                .background(fill, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    level.status == LevelStatus.COMPLETED -> Icons.Filled.Check
                    level.status == LevelStatus.LOCKED -> Icons.Filled.Lock
                    else -> iconFor(level.type)
                },
                contentDescription = "${typeLabel(level.type)}: ${level.title}",
                tint = when (level.status) {
                    LevelStatus.CURRENT -> AppNavy
                    LevelStatus.COMPLETED -> AppWhite
                    LevelStatus.LOCKED -> AppGray
                },
                modifier = Modifier.size(if (level.status == LevelStatus.CURRENT) 30.dp else 26.dp)
            )
        }

        // Type badge for simulations/challenges so they read as different activities.
        if (level.status != LevelStatus.LOCKED &&
            (level.type == LevelType.SIMULATION || level.type == LevelType.CHALLENGE)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .background(AppNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFor(level.type),
                    contentDescription = null,
                    tint = AccentReward,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/** Bonus node — diamond silhouette so rewards read as off-path extras. */
@Composable
fun RewardNode(
    level: LearningLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val locked = level.status == LevelStatus.LOCKED

    val glow = if (!locked) {
        val transition = rememberInfiniteTransition(label = "rewardGlow")
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rewardGlowAlpha"
        ).value
    } else 0f

    Box(
        modifier = modifier.size(NodeSizeReward + 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!locked) {
            Box(
                modifier = Modifier.size(NodeSizeReward + 16.dp).drawBehind {
                    drawCircle(AccentReward.copy(alpha = glow * 0.25f), radius = size.minDimension / 2f)
                }
            )
        }

        Box(
            modifier = Modifier
                .size(NodeSizeReward)
                .background(
                    color = if (locked) AppCard else AccentReward,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (locked) Icons.Filled.Lock else Icons.Filled.Star,
                contentDescription = "Bonus: ${level.title}",
                tint = if (locked) AppGray else AppNavy,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ===========================================================================
// 12. LEVEL PREVIEW BOTTOM SHEET
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelPreviewBottomSheet(
    unit: LearningUnit,
    level: LearningLevel,
    prerequisiteTitle: String?,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppCard,
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                text = "UNIT ${unit.id} · ${typeLabel(level.type)}",
                color = AppCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = level.title,
                color = AppWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (level.status == LevelStatus.LOCKED) {
                    prerequisiteTitle?.let { "Complete \"$it\" first to unlock this level." }
                        ?: "Complete the previous level to unlock this content."
                } else {
                    level.description
                },
                color = AppGray,
                fontSize = 14.sp
            )

            if (level.status != LevelStatus.LOCKED) {
                Spacer(Modifier.height(16.dp))
                Row {
                    MetaChip("${level.durationMinutes} min")
                    Spacer(Modifier.width(10.dp))
                    MetaChip("+${level.xpReward} XP")
                }
            }

            Spacer(Modifier.height(24.dp))

            when (level.status) {
                LevelStatus.LOCKED -> {
                    Button(
                        onClick = onDismiss,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = AppNavy,
                            disabledContentColor = AppGray
                        )
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("LOCKED", fontWeight = FontWeight.Bold)
                    }
                }

                LevelStatus.COMPLETED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = AccentSuccess, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Completed", color = AccentSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue, contentColor = AppWhite)
                    ) {
                        Text("REVIEW", fontWeight = FontWeight.Bold)
                    }
                }

                LevelStatus.CURRENT -> {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
                    ) {
                        Text(
                            text = if (level.type == LevelType.REWARD) "CLAIM REWARD" else "START LEVEL",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .background(AppNavy, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, color = AppWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ===========================================================================
// 13. LEVEL CONTENT REGISTRY
// ===========================================================================

/**
 * What kind of authored content a level opens into. Add a branch per content
 * type as you build new activity formats.
 */
sealed interface LevelContent {
    /** A hands-on simulation the student investigates and acts inside. */
    data class Inbox(val simulation: InboxSimulation) : LevelContent

    /** A scenario-based decision quiz — pick an option, see the consequence. */
    data class Scenarios(val quiz: ScenarioQuiz) : LevelContent

    /** An interactive WebView lab: investigate, submit answers, submit flags. */
    data class Lab(
        val lab: LabDefinition,
        val clueLabels: Map<String, String> = emptyMap()
    ) : LevelContent
}


fun contentFor(levelId: Int): LevelContent? = when (levelId) {
    101 -> LevelContent.Lab(inboxTriageLab(), inboxClueLabels)
    105 -> LevelContent.Scenarios(spotTheThreatQuiz())
    else -> null
}

/** Shown for levels that don't have authored content yet. */
@Composable
fun ComingSoonLevel(
    level: LearningLevel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(AppNavy).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = iconFor(level.type),
            contentDescription = null,
            tint = AppCyan,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(level.title, color = AppWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "This level hasn't been built yet.",
            color = AppGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue, contentColor = AppWhite)
        ) {
            Text("BACK TO PATH", fontWeight = FontWeight.Bold)
        }
    }
}
