package com.cyberity.cvsu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Plays a [Lesson]: its pages, the sources page, the quick check, and a finish
 * screen where the student picks between starting the level and going back to
 * the path. Nothing here costs hearts or XP.
 */
@Composable
fun LessonScreen(
    lesson: Lesson,
    /** "START LEVEL", or "REVIEW LEVEL" for a level already completed. */
    startLabel: String,
    /** False when out of hearts: the level can't be started from here yet. */
    canStart: Boolean,
    modifier: Modifier = Modifier,
    /** Called once, when the finish screen is reached. */
    onRead: () -> Unit,
    onBackToPath: () -> Unit,
    onStartLevel: () -> Unit
) {
    val sourcesStep = lesson.pages.size
    val firstQuestion = sourcesStep + 1
    // Every step before the finish screen: pages, sources, then questions.
    val total = firstQuestion + lesson.questions.size

    var index by rememberSaveable { mutableIntStateOf(0) }
    var answers by rememberSaveable { mutableStateOf(List(lesson.questions.size) { -1 }) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val finished = index == total
    val questionIndex = index - firstQuestion
    val awaitingAnswer = questionIndex in lesson.questions.indices && answers[questionIndex] < 0
    // The option tapped but not yet submitted, so a misclick can be changed.
    // Keyed on the step, so it clears whenever the student moves on or back.
    var pending by rememberSaveable(index) { mutableIntStateOf(-1) }

    LaunchedEffect(finished) { if (finished) onRead() }

    // Leaving mid-lesson asks first; once it's finished there is nothing to lose.
    fun requestLeave() {
        if (finished) onBackToPath() else confirmExit = true
    }

    BackHandler {
        when {
            finished -> onBackToPath()
            index > 0 -> index--
            else -> confirmExit = true
        }
    }

    Column(modifier = modifier.fillMaxSize().background(AppNavy)) {
        Column(modifier = Modifier.fillMaxWidth().background(AppCard)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = ::requestLeave) {
                    Icon(Icons.Filled.Close, contentDescription = "Close lesson", tint = AppGray)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "LESSON", color = AppCyan, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                    Text(lesson.title, color = AppWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                if (!finished) {
                    Text(
                        "${index + 1}/$total", color = AppCyan, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { if (finished) 1f else (index + 1).toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = AppCyan,
                trackColor = AppNavy
            )
        }

        // Keyed so each step starts scrolled to the top.
        key(index) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when {
                    index < sourcesStep -> LessonPageContent(lesson.pages[index], lesson.sources)
                    index == sourcesStep -> LessonSourcesPage(lesson.sources)
                    !finished -> LessonQuestionContent(
                        question = lesson.questions[questionIndex],
                        number = questionIndex + 1,
                        count = lesson.questions.size,
                        chosen = answers[questionIndex],
                        selected = pending,
                        onSelect = { pending = it }
                    )
                    else -> LessonFinish(
                        correct = lesson.questions.indices.count { answers[it] == lesson.questions[it].correctIndex },
                        count = lesson.questions.size,
                        canStart = canStart
                    )
                }
            }
        }

        if (finished) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Button(
                    onClick = onStartLevel,
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppCyan,
                        contentColor = AppNavy,
                        disabledContainerColor = AppCard,
                        disabledContentColor = AppGray
                    )
                ) {
                    Text(if (canStart) startLabel else "OUT OF HEARTS", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBackToPath,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppWhite)
                ) {
                    Text("BACK TO PATH", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                if (index > 0) {
                    OutlinedButton(
                        onClick = { index-- },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AppBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppWhite)
                    ) {
                        Text("BACK", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Button(
                    onClick = {
                        if (awaitingAnswer) {
                            answers = answers.toMutableList().also { it[questionIndex] = pending }
                        } else {
                            index++
                        }
                    },
                    enabled = !awaitingAnswer || pending >= 0,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppCyan,
                        contentColor = AppNavy,
                        disabledContainerColor = AppCard,
                        disabledContentColor = AppGray
                    )
                ) {
                    Text(
                        when {
                            awaitingAnswer && pending < 0 -> "PICK AN ANSWER"
                            awaitingAnswer -> "SUBMIT ANSWER"
                            index == sourcesStep -> "QUICK CHECK"
                            index == total - 1 -> "FINISH"
                            else -> "NEXT"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            containerColor = AppCard,
            title = { Text("Leave the lesson?", color = AppWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You'll go back to the path. You can read it again from the level card.",
                    color = AppGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmExit = false
                    onBackToPath()
                }) {
                    Text("LEAVE", color = AppDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmExit = false }) {
                    Text("STAY", color = AppGray)
                }
            }
        )
    }
}

private fun iconFor(icon: LessonIcon): ImageVector = when (icon) {
    LessonIcon.PHISHING -> Icons.Filled.Phishing
    LessonIcon.WARNING -> Icons.Filled.NotificationImportant
    LessonIcon.EMAIL -> Icons.Filled.AlternateEmail
    LessonIcon.LINK -> Icons.Filled.Link
    LessonIcon.CODE -> Icons.Filled.Code
    LessonIcon.REPORT -> Icons.Filled.Report
    LessonIcon.SHIELD -> Icons.Filled.Shield
    LessonIcon.SEARCH -> Icons.Filled.Search
    LessonIcon.LOCK -> Icons.Filled.Lock
}

// ===========================================================================
// PAGES
// ===========================================================================

/** Look first, then read why: icon and title, the example, the text, the terms. */
@Composable
private fun LessonPageContent(page: LessonPage, sources: List<LessonSource>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).background(AppBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconFor(page.icon), contentDescription = null, tint = AppOnBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        LessonChip(page.task?.let { "PREPARES YOU FOR TASK $it" } ?: "INTRODUCTION")
    }
    Spacer(Modifier.height(14.dp))
    Text(page.title, color = AppWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)

    page.visual?.let { visual ->
        Spacer(Modifier.height(16.dp))
        when (visual) {
            is LessonVisual.Email -> EmailVisual(visual)
            is LessonVisual.Address -> AddressVisual(visual)
            is LessonVisual.Link -> LinkVisual(visual)
            is LessonVisual.Source -> SourceVisual(visual)
            is LessonVisual.DoDont -> DoDontVisual(visual)
            is LessonVisual.Log -> LogVisual(visual)
        }
    }

    page.paragraphs.forEach { paragraph ->
        Spacer(Modifier.height(14.dp))
        Text(emphasised(paragraph), color = AppGray, fontSize = 15.sp, lineHeight = 23.sp)
    }

    if (page.keyTerms.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        KeyTermsBox(page.keyTerms)
    }

    val cited = page.sourceIds.mapNotNull { id -> sources.firstOrNull { it.id == id }?.short }.distinct()
    if (cited.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        Text(
            "Source: ${cited.joinToString(" · ")}",
            color = AppGray, fontSize = 12.sp, fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LessonSourcesPage(sources: List<LessonSource>) {
    val uriHandler = LocalUriHandler.current

    LessonChip("REFERENCES")
    Spacer(Modifier.height(14.dp))
    Text("Sources", color = AppWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Text(
        "This lesson is paraphrased from the publications below. Tap one to open it. " +
                "Next: a quick check with nothing at stake.",
        color = AppGray, fontSize = 14.sp, lineHeight = 21.sp
    )

    sources.forEach { source ->
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppCard, RoundedCornerShape(12.dp))
                // A device with no browser shouldn't crash the lesson.
                .clickable { runCatching { uriHandler.openUri(source.url) } }
                .padding(14.dp)
        ) {
            Text(source.citation, color = AppWhite, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                source.url, color = AppCyan, fontSize = 11.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ===========================================================================
// QUICK CHECK
// ===========================================================================

@Composable
private fun LessonQuestionContent(
    question: LessonQuestion,
    number: Int,
    count: Int,
    /** The submitted answer, or -1 before SUBMIT ANSWER. */
    chosen: Int,
    /** The option tapped but not yet submitted, or -1. */
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val answered = chosen >= 0

    LessonChip("QUICK CHECK · $number OF $count")
    Spacer(Modifier.height(8.dp))
    Text("No hearts or XP at stake.", color = AppGray, fontSize = 12.sp)
    Spacer(Modifier.height(14.dp))
    Text(question.prompt, color = AppWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp)

    question.options.forEachIndexed { i, option ->
        val isCorrect = i == question.correctIndex
        val isSelected = !answered && i == selected
        val outline = when {
            answered && isCorrect -> AppSuccess
            answered && i == chosen -> AppDanger
            isSelected -> AppCyan
            else -> AppBorder
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppCard, RoundedCornerShape(12.dp))
                .border(if (isSelected) 2.dp else 1.dp, outline, RoundedCornerShape(12.dp))
                .clickable(enabled = !answered) { onSelect(i) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(26.dp).background(if (isSelected) AppCyan else AppNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ('A' + i).toString(), color = if (isSelected) AppNavy else AppCyan,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                option,
                color = if (answered && !isCorrect && i != chosen) AppGray else AppWhite,
                fontSize = 14.sp, lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
            if (answered && (isCorrect || i == chosen)) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = if (isCorrect) "Correct answer" else "Your answer",
                    tint = outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (answered) {
        val right = chosen == question.correctIndex
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppCard, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                if (right) "Correct" else "Not quite",
                color = if (right) AppSuccess else AppDanger,
                fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(question.explanation, color = AppGray, fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun LessonFinish(correct: Int, count: Int, canStart: Boolean) {
    Spacer(Modifier.height(16.dp))
    Box(
        modifier = Modifier.size(84.dp).background(AppBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = AppOnBlue, modifier = Modifier.size(40.dp))
    }
    Spacer(Modifier.height(20.dp))
    Text("Lesson complete", color = AppWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("You got $correct of $count right.", color = AppCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Text(
        when {
            correct == count -> "You're ready for the lab."
            else -> "Reread it any time with READ LESSON on the level card — the lab also " +
                    "explains each task as you go."
        },
        color = AppGray, fontSize = 15.sp, lineHeight = 22.sp
    )
    Spacer(Modifier.height(12.dp))
    Text(
        if (canStart) "Start the level now, or head back to the path and start it later."
        else "You're out of hearts, so the level has to wait. Your lesson is saved.",
        color = AppGray, fontSize = 14.sp, lineHeight = 21.sp
    )
}

// ===========================================================================
// VISUALS
// ===========================================================================

@Composable
private fun EmailVisual(email: LessonVisual.Email) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(AppNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(email.senderName.take(1), color = AppCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(email.senderName, color = AppWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(email.senderAddress, color = AppGray, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(email.subject, color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(emphasised(email.body), color = AppGray, fontSize = 14.sp, lineHeight = 20.sp)
        email.button?.let {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(AppBlue, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(it, color = AppOnBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    Text(
        "WARNING SIGNS", color = AppDanger, fontSize = 11.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
    )
    email.warningSigns.forEach { sign ->
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = AppDanger, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(sign, color = AppWhite, fontSize = 14.sp)
        }
    }
}

@Composable
private fun AddressVisual(address: LessonVisual.Address) {
    // The whole address as an inbox would show it, then its parts one by one.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = AppGray)) { append("${address.displayName} <") }
                withStyle(SpanStyle(color = AppWhite)) { append("${address.username}@") }
                withStyle(SpanStyle(color = AppCyan, fontWeight = FontWeight.Bold)) { append(address.domain) }
                withStyle(SpanStyle(color = AppGray)) { append(">") }
            },
            fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 19.sp
        )
        Spacer(Modifier.height(14.dp))
        AddressPart("DISPLAY NAME", address.displayName, "Typed by the sender. Can say anything.", trusted = false)
        Spacer(Modifier.height(12.dp))
        AddressPart("DOMAIN", address.domain, "Where it really came from. Read this part.", trusted = true)
    }
}

@Composable
private fun AddressPart(label: String, value: String, note: String, trusted: Boolean) {
    val tint = if (trusted) AppSuccess else AppDanger
    Row {
        Icon(
            if (trusted) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null, tint = tint, modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(value, color = AppWhite, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            Text(note, color = AppGray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun LinkVisual(link: LessonVisual.Link) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("THE LINK SAYS", color = AppGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text(link.shownText, color = AppCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        Icon(
            Icons.Filled.ArrowDownward, contentDescription = null, tint = AppGray,
            modifier = Modifier.padding(vertical = 6.dp).size(18.dp)
        )
        Text("IT REALLY GOES TO", color = AppDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text(link.realDestination, color = AppWhite, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }

    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = AppCyan, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("The padlock (HTTPS)", color = AppWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        MeaningRow(true, "Means the connection is encrypted")
        Spacer(Modifier.height(6.dp))
        MeaningRow(false, "Doesn't mean the site is who it claims to be")
    }
}

@Composable
private fun MeaningRow(yes: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (yes) Icons.Filled.Check else Icons.Filled.Close, contentDescription = null,
            tint = if (yes) AppSuccess else AppDanger, modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = AppGray, fontSize = 13.sp)
    }
}

@Composable
private fun SourceVisual(source: LessonVisual.Source) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        source.lines.forEach { line ->
            val comment = line.trimStart().startsWith("<!--")
            Text(
                line,
                color = if (comment) AppGray else AppWhite,
                fontSize = 12.sp, lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (comment) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.VisibilityOff, contentDescription = null, tint = AppGray, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Grey lines are comments — never drawn on screen.", color = AppGray, fontSize = 12.sp)
    }
}

@Composable
private fun LogVisual(log: LessonVisual.Log) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(log.title, color = AppGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        log.lines.forEachIndexed { i, line ->
            val marked = i in log.highlights
            Text(
                line,
                color = if (marked) AppCyan else AppWhite,
                fontSize = 11.sp, lineHeight = 17.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (marked) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = AppCyan, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(log.caption, color = AppGray, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun DoDontVisual(visual: LessonVisual.DoDont) {
    DoDontCard("DO", visual.dos, AppSuccess, Icons.Filled.Check)
    Spacer(Modifier.height(12.dp))
    DoDontCard("DON'T", visual.donts, AppDanger, Icons.Filled.Close)
}

@Composable
private fun DoDontCard(label: String, items: List<String>, tint: Color, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, tint, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        items.forEach { item ->
            Spacer(Modifier.height(8.dp))
            Row {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                Spacer(Modifier.width(10.dp))
                Text(item, color = AppWhite, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun KeyTermsBox(terms: List<KeyTerm>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(14.dp))
            .border(1.dp, AppCyan, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("KEY TERMS", color = AppCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        terms.forEach { term ->
            Spacer(Modifier.height(10.dp))
            Text(term.term, color = AppWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(term.meaning, color = AppGray, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun LessonChip(text: String) {
    Box(
        modifier = Modifier
            .background(AppCard, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text, color = AppCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
