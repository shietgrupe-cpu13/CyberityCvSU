package com.cyberity.cvsu

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

// ===========================================================================
// 1. PALETTE (file-scoped)
// ===========================================================================

private val LabGreen = Color(0xFF27E0A8)
private val LabRed = Color(0xFFFF5C7A)

private enum class LabStage { BRIEFING, RUNNING, RESULT }

private enum class Verdict { CORRECT, INCORRECT }

// ===========================================================================
// 2. JS BRIDGE
// ===========================================================================

/**
 * The only surface the simulation can reach. Four notify* methods, each one
 * doing nothing but recording a clue id — no file access, no navigation, no
 * Android APIs exposed to the page.
 *
 * WebView calls these on a background thread, so every callback is posted back
 * to the main thread before it touches Compose state.
 */
class LabBridge(private val onClue: (String) -> Unit) {

    private val main = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun notifyClueFound(clueId: String) {
        main.post { onClue(clueId) }
    }

    @JavascriptInterface
    fun notifyEmailOpened(emailId: String) {
        main.post { onClue("email_opened_$emailId") }
    }

    @JavascriptInterface
    fun notifyUrlInspected(url: String) {
        main.post { onClue("url_inspected") }
    }

    @JavascriptInterface
    fun notifyFlagDiscovered(token: String) {
        main.post { onClue("flag_seen") }
    }
}

// ===========================================================================
// 3. SCREEN
// ===========================================================================

/**
 * Runs a [LabDefinition] end to end: briefing → tasks against a live WebView
 * simulation → result. Generic — any level with an assets folder and a task
 * list can use it.
 */
@Composable
fun LabScreen(
    lab: LabDefinition,
    xpReward: Int,
    onExit: () -> Unit,
    onComplete: (xpEarned: Int, tasksSolved: Int, totalTasks: Int) -> Unit,
    onMistake: () -> Unit,
    hearts: Int,
    xpBalance: Int,
    onSpendXp: (Int) -> Unit,
    modifier: Modifier = Modifier,
    clueLabels: Map<String, String> = emptyMap()
) {
    var stage by remember { mutableStateOf(LabStage.BRIEFING) }
    var taskIndex by remember { mutableIntStateOf(0) }
    var solved by remember { mutableIntStateOf(0) }
    var verdict by remember { mutableStateOf<Verdict?>(null) }
    var answerText by remember { mutableStateOf("") }
    var choiceIndex by remember { mutableStateOf<Int?>(null) }
    var panelExpanded by remember { mutableStateOf(true) }

    // Evidence discovered in the simulation, in the order it was found.
    val clues = remember { mutableStateListOf<String>() }
    // Hints opened, per task id.
    val hintsOpened = remember { mutableStateMapOf<String, Int>() }
    // Mistakes made this attempt — decides the no-heart-lost bonus.
    var heartsLost by remember { mutableIntStateOf(0) }
    // XP already taken from the balance for hints this attempt, for the result breakdown.
    var hintsPaid by remember { mutableIntStateOf(0) }

    var webView by remember { mutableStateOf<WebView?>(null) }

    // Dangerous actions cost a heart, once each, and are never logged as evidence.
    val penalised = remember { mutableStateListOf<String>() }

    val bridge = remember {
        LabBridge { clueId ->
            when {
                clueId in lab.dangerousClues -> if (clueId !in penalised) {
                    penalised.add(clueId)
                    heartsLost++
                    onMistake()
                }
                clueId !in clues -> clues.add(clueId)
            }
        }
    }

    val tasks = lab.tasks
    val total = tasks.size
    val task = tasks[taskIndex.coerceIn(0, total - 1)]
    val hintsUsedTotal = hintsOpened.values.sum()

    // Evidence worth finding: every labelled clue that isn't a dangerous action.
    val discoverableClues = clueLabels.keys - lab.dangerousClues.toSet()
    val allCluesFound = discoverableClues.isEmpty() || clues.containsAll(discoverableClues)

    // What finishing right now would pay out. Hints aren't deducted here — they
    // were charged to the balance when opened — but they do forfeit the bonus.
    val liveXp = scoreLevelXp(
        completed = true,
        heartsLost = heartsLost,
        hintsUsed = hintsUsedTotal,
        allCluesFound = allCluesFound
    )

    // Price of this task's next hint, and whether the student can afford it.
    val hintCost = nextHintCost(hintsOpened[task.id] ?: 0)
    val canAffordHint = xpBalance >= hintCost


    // Load the page a task starts on. Keyed on the task, so it fires once per task.
    LaunchedEffect(taskIndex, webView) {
        val page = tasks[taskIndex].entryPage ?: return@LaunchedEffect
        webView?.loadUrl(lab.baseUrl + page)
    }

    BackHandler(enabled = stage == LabStage.RUNNING) {
        val view = webView
        if (view != null && view.canGoBack()) view.goBack() else onExit()
    }

    fun submit() {
        val correct = LabValidator.isCorrect(task.answer, answerText, choiceIndex)
        verdict = if (correct) Verdict.CORRECT else Verdict.INCORRECT
        if (correct) solved++ else {
            heartsLost++
            onMistake()
        }
        panelExpanded = true
    }

    fun nextTask() {
        verdict = null
        answerText = ""
        choiceIndex = null
        if (taskIndex == total - 1) stage = LabStage.RESULT else taskIndex++
    }

    Box(modifier = modifier.fillMaxSize().background(AppNavy)) {
        when (stage) {

            LabStage.BRIEFING -> LabBriefing(
                lab = lab,
                xpReward = xpReward,
                onExit = onExit,
                onBegin = { stage = LabStage.RUNNING }
            )

            LabStage.RUNNING -> Column(modifier = Modifier.fillMaxSize()) {

                LabTopBar(
                    lab = lab,
                    taskNumber = taskIndex + 1,
                    total = total,
                    solved = solved,
                    hearts = hearts,
                    liveXp = liveXp,
                    xpBalance = xpBalance,
                    onExit = onExit
                )

                TaskPanel(
                    task = task,
                    taskNumber = taskIndex + 1,
                    total = total,
                    clues = clues,
                    clueLabels = clueLabels,
                    hintsOpened = hintsOpened[task.id] ?: 0,
                    hintCost = hintCost,
                    canAffordHint = canAffordHint,
                    onOpenHint = {
                        if (canAffordHint) {
                            hintsOpened[task.id] = (hintsOpened[task.id] ?: 0) + 1
                            hintsPaid += hintCost
                            onSpendXp(hintCost)
                        }
                    },
                    verdict = verdict,
                    answerText = answerText,
                    onAnswerChange = { answerText = it },
                    choiceIndex = choiceIndex,
                    onChoose = { choiceIndex = it },
                    onSubmit = { submit() },
                    onRetry = { verdict = null },
                    onNext = { nextTask() },
                    expanded = panelExpanded,
                    onToggle = { panelExpanded = !panelExpanded },
                    isLast = taskIndex == total - 1
                )

                SimulationWebView(
                    lab = lab,
                    bridge = bridge,
                    onCreated = { webView = it },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            LabStage.RESULT -> {
                val award = awardFor(
                    completed = solved == total,
                    heartsLost = heartsLost,
                    hintsUsed = hintsUsedTotal,
                    allCluesFound = allCluesFound
                )
                LabResult(
                    lab = lab,
                    solved = solved,
                    total = total,
                    cluesFound = clues.size,
                    hintsUsed = hintsUsedTotal,
                    heartsLost = heartsLost,
                    hintsPaid = hintsPaid,
                    award = award,
                    onFinish = { onComplete(award.total, solved, total) }
                )
            }
        }
    }
}

// ===========================================================================
// 4. THE WEBVIEW
// ===========================================================================

/**
 * The simulation itself. Locked down: JavaScript is on because the lab needs
 * it, everything else is off, and navigation outside the bundled assets is
 * refused — so no page in here can ever reach the network.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SimulationWebView(
    lab: LabDefinition,
    bridge: LabBridge,
    onCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                settings.setSupportZoom(false)
                settings.builtInZoomControls = false
                isVerticalScrollBarEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        // true = we handled it = don't load. Only bundled pages pass.
                        return !request.url.toString().startsWith(LAB_ASSET_ROOT)
                    }
                }

                addJavascriptInterface(bridge, "AndroidLab")
                loadUrl(lab.baseUrl + lab.startPage)
                onCreated(this)
            }
        },
        onRelease = { it.destroy() }
    )
}

// ===========================================================================
// 5. BRIEFING
// ===========================================================================

@Composable
private fun LabBriefing(
    lab: LabDefinition,
    xpReward: Int,
    onExit: () -> Unit,
    onBegin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onExit) {
            Icon(Icons.Filled.Close, contentDescription = "Exit lab", tint = AppGray)
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(84.dp).background(AppBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Email, contentDescription = null, tint = AppWhite,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            lab.subtitle.uppercase(), color = AppCyan, fontSize = 12.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(lab.title, color = AppWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(lab.briefing, color = AppGray, fontSize = 15.sp, lineHeight = 22.sp)

        Spacer(Modifier.height(20.dp))
        Row {
            LabChip("${lab.tasks.size} tasks")
            Spacer(Modifier.width(10.dp))
            LabChip("up to +$xpReward XP")
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onBegin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
        ) {
            Text("ENTER LAB", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun LabChip(text: String) {
    Box(
        modifier = Modifier
            .background(AppCard, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text, color = AppCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ===========================================================================
// 6. TOP BAR
// ===========================================================================

@Composable
private fun LabTopBar(
    lab: LabDefinition,
    taskNumber: Int,
    total: Int,
    solved: Int,
    hearts: Int,
    liveXp: Int,
    xpBalance: Int,
    onExit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(AppCard)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit lab", tint = AppGray)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(lab.title, color = AppWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(lab.subtitle, color = AppGray, fontSize = 11.sp)
            }
            Text(
                "$solved/$total", color = AppCyan, fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeartsRow(hearts = hearts)
            Spacer(Modifier.weight(1f))
            // Balance first — it moves when a hint is bought — then what this
            // level would add to it.
            XpIndicator(xp = xpBalance, iconSize = 16.dp, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                "+$liveXp", color = AppCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { if (total == 0) 0f else solved.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = AppCyan,
            trackColor = AppNavy
        )
    }
}

// ===========================================================================
// 7. TASK PANEL
// ===========================================================================

@Composable
private fun TaskPanel(
    task: LabTask,
    taskNumber: Int,
    total: Int,
    clues: List<String>,
    clueLabels: Map<String, String>,
    hintsOpened: Int,
    hintCost: Int,
    canAffordHint: Boolean,
    onOpenHint: () -> Unit,
    verdict: Verdict?,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    choiceIndex: Int?,
    onChoose: (Int) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
    isLast: Boolean
) {
    val unlocked = task.requiredClues.all { it in clues }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppNavy)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "TASK $taskNumber OF $total", color = AppCyan, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(task.title, color = AppWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Hide task" else "Show task",
                tint = AppGray
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)) + expandVertically(tween(180))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 330.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))
                Text(task.objective, color = AppGray, fontSize = 13.sp, lineHeight = 19.sp)

                Spacer(Modifier.height(10.dp))
                EvidenceStrip(clues = clues, clueLabels = clueLabels)

                when (verdict) {
                    Verdict.CORRECT -> {
                        Spacer(Modifier.height(12.dp))
                        FeedbackCard(
                            correct = true,
                            heading = "Task complete",
                            body = task.successFeedback
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onNext,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppCyan, contentColor = AppNavy
                            )
                        ) {
                            Text(
                                if (isLast) "FINISH LAB" else "NEXT TASK",
                                fontWeight = FontWeight.Bold, fontSize = 13.sp
                            )
                        }
                    }

                    Verdict.INCORRECT -> {
                        Spacer(Modifier.height(12.dp))
                        FeedbackCard(
                            correct = false,
                            heading = "Not it",
                            body = task.failureFeedback
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppCard, contentColor = AppWhite
                            )
                        ) {
                            Text("KEEP INVESTIGATING", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    null -> {
                        HintSection(
                            hints = task.hints,
                            opened = hintsOpened,
                            cost = hintCost,
                            affordable = canAffordHint,
                            onOpenHint = onOpenHint
                        )

                        Spacer(Modifier.height(12.dp))

                        if (!unlocked) {
                            LockedNotice(task.lockedMessage)
                        } else {
                            AnswerArea(
                                answer = task.answer,
                                answerText = answerText,
                                onAnswerChange = onAnswerChange,
                                choiceIndex = choiceIndex,
                                onChoose = onChoose,
                                onSubmit = onSubmit
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/** Live evidence log — proof that investigating is what moves the level. */
@Composable
private fun EvidenceStrip(clues: List<String>, clueLabels: Map<String, String>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = AppCyan, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (clues.isEmpty()) {
                "No evidence yet — start opening things below"
            } else {
                "${clues.size} found · ${clueLabels[clues.last()] ?: clues.last()}"
            },
            color = if (clues.isEmpty()) AppGray else AppCyan,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun LockedNotice(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(12.dp))
            .border(1.dp, AppGray.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = AppGray, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, color = AppGray, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun HintSection(
    hints: List<String>,
    opened: Int,
    cost: Int,
    affordable: Boolean,
    onOpenHint: () -> Unit
) {
    if (hints.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        hints.take(opened).forEachIndexed { index, hint ->
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBlue.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                    .border(1.dp, AppBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = AppCyan, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Hint ${index + 1}", color = AppCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(hint, color = AppWhite, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }

        if (opened < hints.size) {
            TextButton(
                onClick = onOpenHint,
                enabled = affordable,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = if (affordable) {
                        if (opened == 0) "Need a hint? (-$cost XP)" else "Another hint (-$cost XP)"
                    } else {
                        "Hint locked - needs $cost XP"
                    },
                    color = if (affordable) AppCyan else AppGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ===========================================================================
// 8. ANSWER AREA
// ===========================================================================

@Composable
private fun AnswerArea(
    answer: LabAnswer,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    choiceIndex: Int?,
    onChoose: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    when (answer) {

        is LabAnswer.Choice -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                answer.options.forEachIndexed { index, option ->
                    val selected = choiceIndex == index
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(
                                if (selected) AppCyan.copy(alpha = 0.14f) else AppCard,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (selected) AppCyan else AppGray.copy(alpha = 0.22f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onChoose(index) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option,
                            color = if (selected) AppWhite else AppGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = AppCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                SubmitButton(
                    label = "SUBMIT ANSWER",
                    enabled = choiceIndex != null,
                    onClick = onSubmit
                )
            }
        }

        is LabAnswer.Text -> Column(modifier = Modifier.fillMaxWidth()) {
            LabTextField(
                value = answerText,
                onValueChange = onAnswerChange,
                placeholder = answer.placeholder,
                monospace = true
            )
            Spacer(Modifier.height(10.dp))
            SubmitButton(
                label = "SUBMIT ANSWER",
                enabled = answerText.isNotBlank(),
                onClick = onSubmit
            )
        }

        is LabAnswer.Flag -> Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "SUBMIT FLAG", color = AppCyan, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            LabTextField(
                value = answerText,
                onValueChange = onAnswerChange,
                placeholder = answer.placeholder,
                monospace = true
            )
            Spacer(Modifier.height(10.dp))
            SubmitButton(
                label = "SUBMIT FLAG",
                enabled = answerText.isNotBlank(),
                onClick = onSubmit
            )
        }
    }
}

@Composable
private fun LabTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    monospace: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder, color = AppGray.copy(alpha = 0.7f), fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = AppWhite,
            fontSize = 14.sp,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppCyan,
            unfocusedBorderColor = AppGray.copy(alpha = 0.35f),
            focusedContainerColor = AppCard,
            unfocusedContainerColor = AppCard,
            cursorColor = AppCyan,
            focusedTextColor = AppWhite,
            unfocusedTextColor = AppWhite
        )
    )
}

@Composable
private fun SubmitButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppCyan,
            contentColor = AppNavy,
            disabledContainerColor = AppCard,
            disabledContentColor = AppGray
        )
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// ===========================================================================
// 9. FEEDBACK
// ===========================================================================

@Composable
private fun FeedbackCard(correct: Boolean, heading: String, body: String) {
    val accent = if (correct) LabGreen else LabRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (correct) Icons.Filled.Check else Icons.Filled.Warning,
                contentDescription = null, tint = accent, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(heading, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(body, color = AppWhite, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

// ===========================================================================
// 10. RESULT
// ===========================================================================

@Composable
private fun LabResult(
    lab: LabDefinition,
    solved: Int,
    total: Int,
    cluesFound: Int,
    hintsUsed: Int,
    heartsLost: Int,
    hintsPaid: Int,
    award: XpAward,
    onFinish: () -> Unit
) {
    val passed = solved == total
    val accent = if (passed) LabGreen else LabRed

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
                contentDescription = null, tint = accent, modifier = Modifier.size(46.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = if (passed) "Lab complete" else "Lab closed",
            color = AppWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = lab.title,
            color = AppGray, fontSize = 14.sp, textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResultStat("$solved/$total", "Tasks")
            ResultStat("$cluesFound", "Evidence")
            ResultStat("$hintsUsed", "Hints")
        }

        Spacer(Modifier.height(24.dp))
        XpBreakdown(
            award = award,
            heartsLost = heartsLost,
            hintsUsed = hintsUsed,
            hintsPaid = hintsPaid
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
        ) {
            Text("BACK TO PATH", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = AppWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = AppGray, fontSize = 11.sp)
    }
}
