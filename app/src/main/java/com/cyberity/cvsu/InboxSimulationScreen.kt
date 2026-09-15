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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===========================================================================
// 1. MODEL
// ===========================================================================

/** What kind of artifact the student is inspecting — drives icon and label. */
enum class ArtifactKind { SENDER, LINK, ATTACHMENT, GREETING, TONE, REPLY_TO }

/**
 * A single inspectable piece of an email.
 *
 * [surface] is what the mail client shows at a glance — usually the part
 * designed to look trustworthy. [hidden] is what inspecting it uncovers.
 * The gap between the two is the entire lesson.
 */
@Immutable
data class Artifact(
    val id: String,
    val kind: ArtifactKind,
    val surface: String,
    val hidden: String,
    val isRedFlag: Boolean,
    val explanation: String
)

@Immutable
data class SimEmail(
    val id: String,
    val senderDisplay: String,
    val subject: String,
    val receivedAt: String,
    val bodyParagraphs: List<String>,
    val artifacts: List<Artifact>,
    val isPhishing: Boolean,
    /** Shown in the debrief once the student has committed to a verdict. */
    val debrief: String
) {
    val redFlagCount: Int get() = artifacts.count { it.isRedFlag }
}

@Immutable
data class InboxSimulation(
    val levelId: Int,
    val title: String,
    val briefing: String,
    val emails: List<SimEmail>
)

/** Per-email outcome, accumulated across the run. */
@Immutable
data class TriageResult(
    val emailId: String,
    val verdictCorrect: Boolean,
    val flagsFound: Int,
    val flagsTotal: Int
)

// ===========================================================================
// 2. LEVEL 101 CONTENT — Inbox Triage
// ===========================================================================

fun inboxTriageSimulation(): InboxSimulation = InboxSimulation(
    levelId = 101,
    title = "Inbox Triage",
    briefing = "You're reviewing three messages from a student inbox. Tap any part of a " +
            "message to inspect what's underneath it, then decide: report it, or let it through. " +
            "Inspect carefully — not every message is hostile, and wrongly reporting a real " +
            "one has a cost too.",
    emails = listOf(
        SimEmail(
            id = "m1",
            senderDisplay = "CvSU Student Portal",
            subject = "URGENT: Your account will be deleted in 24 hours",
            receivedAt = "04:17",
            bodyParagraphs = listOf(
                "Dear User,",
                "We detected unusual activity on your student account. For your protection, " +
                        "your account is scheduled for permanent deletion within 24 hours.",
                "To keep your account active, verify your identity immediately using the " +
                        "secure link below. Failure to verify will result in loss of all " +
                        "enrollment records."
            ),
            artifacts = listOf(
                Artifact(
                    id = "m1a1",
                    kind = ArtifactKind.SENDER,
                    surface = "CvSU Student Portal",
                    hidden = "cvsu-portal-verify@gmail.com",
                    isRedFlag = true,
                    explanation = "The display name is free text — anyone can set it to anything. " +
                            "The real address is a public gmail account. A university system " +
                            "would send from an address ending in cvsu.edu.ph."
                ),
                Artifact(
                    id = "m1a2",
                    kind = ArtifactKind.LINK,
                    surface = "Verify My Account",
                    hidden = "http://cvsu-portal.verify-login.tk/auth?id=8823",
                    isRedFlag = true,
                    explanation = "The real domain is verify-login.tk — \"cvsu-portal\" is just a " +
                            "subdomain an attacker chose. Read domains right-to-left: the part " +
                            "before the final slash is what actually owns the page. Also note http, not https."
                ),
                Artifact(
                    id = "m1a3",
                    kind = ArtifactKind.GREETING,
                    surface = "Dear User,",
                    hidden = "No name, no student number, no course",
                    isRedFlag = true,
                    explanation = "A real portal message knows who you are. Generic greetings " +
                            "mean the same message went to thousands of addresses at once."
                ),
                Artifact(
                    id = "m1a4",
                    kind = ArtifactKind.TONE,
                    surface = "deleted in 24 hours",
                    hidden = "Manufactured deadline + threat of losing records",
                    isRedFlag = true,
                    explanation = "Urgency exists to stop you from checking. Legitimate " +
                            "institutions give notice and never threaten immediate destruction of your data."
                )
            ),
            isPhishing = true,
            debrief = "Four independent red flags. Any one of them is enough to report — " +
                    "you never need to find all of them before acting."
        ),
        SimEmail(
            id = "m2",
            senderDisplay = "CvSU Office of the Registrar",
            subject = "Enrollment schedule — 2nd Semester A.Y. 2026–2027",
            receivedAt = "Yesterday",
            bodyParagraphs = listOf(
                "Good day, Juan Dela Cruz (2023-00412),",
                "The enrollment schedule for the second semester has been released. Your " +
                        "assigned enrollment window is based on your year level.",
                "You may view the full schedule on the registrar's page. No action is " +
                        "required from you at this time."
            ),
            artifacts = listOf(
                Artifact(
                    id = "m2a1",
                    kind = ArtifactKind.SENDER,
                    surface = "CvSU Office of the Registrar",
                    hidden = "registrar@cvsu.edu.ph",
                    isRedFlag = false,
                    explanation = "The address sits on the university's own domain. This is what " +
                            "a genuine institutional sender looks like."
                ),
                Artifact(
                    id = "m2a2",
                    kind = ArtifactKind.LINK,
                    surface = "view the full schedule",
                    hidden = "https://cvsu.edu.ph/registrar/enrollment-schedule",
                    isRedFlag = false,
                    explanation = "The final domain is cvsu.edu.ph, served over https. The link " +
                            "goes where it claims to go."
                ),
                Artifact(
                    id = "m2a3",
                    kind = ArtifactKind.GREETING,
                    surface = "Good day, Juan Dela Cruz (2023-00412),",
                    hidden = "Full name and student number both correct",
                    isRedFlag = false,
                    explanation = "The sender holds information only the real system would have. " +
                            "That's a strong signal of authenticity."
                ),
                Artifact(
                    id = "m2a4",
                    kind = ArtifactKind.TONE,
                    surface = "No action is required",
                    hidden = "Informational, no deadline, nothing to click urgently",
                    isRedFlag = false,
                    explanation = "Notice how different this feels. Real notices inform; " +
                            "phishing demands."
                )
            ),
            isPhishing = false,
            debrief = "This one is genuine. Reporting real messages has a cost — people who " +
                    "flag everything get ignored, and real deadlines get missed."
        ),
        SimEmail(
            id = "m3",
            senderDisplay = "Scholarship Grants Office",
            subject = "Congratulations! Your ₱25,000 grant has been approved",
            receivedAt = "02:48",
            bodyParagraphs = listOf(
                "Hi student,",
                "Great news! You have been selected as a recipient of our financial " +
                        "assistance grant worth ₱25,000. No application was needed — you were " +
                        "chosen automatically.",
                "Download and complete the attached claim form, then reply with your bank " +
                        "details so we can release the funds within 24 hours."
            ),
            artifacts = listOf(
                Artifact(
                    id = "m3a1",
                    kind = ArtifactKind.SENDER,
                    surface = "Scholarship Grants Office",
                    hidden = "scholarship.cvsu.claims@outlook.com",
                    isRedFlag = true,
                    explanation = "Putting \"cvsu\" inside the username changes nothing — the " +
                            "domain is outlook.com. Attackers include familiar words to make " +
                            "the address skim as legitimate."
                ),
                Artifact(
                    id = "m3a2",
                    kind = ArtifactKind.ATTACHMENT,
                    surface = "Scholarship_Claim_Form.pdf",
                    hidden = "Scholarship_Claim_Form.pdf.exe  (2.4 MB executable)",
                    isRedFlag = true,
                    explanation = "A double extension. The system hides the real .exe ending, so " +
                            "it displays as a PDF. Opening it runs a program, it doesn't open a document."
                ),
                Artifact(
                    id = "m3a3",
                    kind = ArtifactKind.REPLY_TO,
                    surface = "Reply-To header",
                    hidden = "grants.payout.department@mail.ru",
                    isRedFlag = true,
                    explanation = "Your reply would go somewhere else entirely. Sender and " +
                            "reply-to disagreeing is a deliberate redirect."
                ),
                Artifact(
                    id = "m3a4",
                    kind = ArtifactKind.TONE,
                    surface = "reply with your bank details",
                    hidden = "Unsolicited prize + request for financial credentials",
                    isRedFlag = true,
                    explanation = "You didn't apply for this. An award you never applied for, " +
                            "combined with a request for bank details, is an advance-fee scam."
                )
            ),
            isPhishing = true,
            debrief = "Classic advance-fee lure. The double-extension attachment is the most " +
                    "dangerous element — it executes code, it doesn't just harvest credentials."
        )
    )
)

// ===========================================================================
// 3. PALETTE (file-scoped)
// ===========================================================================

private val OkGreen = Color(0xFF27E0A8)
private val FlagRed = Color(0xFFFF5C7A)

// ===========================================================================
// 4. SCREEN
// ===========================================================================

private enum class InboxStage { BRIEFING, READING, DEBRIEF, RESULT }

/**
 * An interactive inbox-triage simulation.
 *
 * The student inspects artifacts inside each message to uncover hidden state
 * (real sender address, true link destination, reply-to redirect, double file
 * extension), then commits to a verdict. Scoring rewards both the correct
 * decision and the evidence actually gathered before making it.
 */
@Composable
fun InboxSimulationScreen(
    simulation: InboxSimulation,
    xpReward: Int,
    onExit: () -> Unit,
    onComplete: (xpEarned: Int, correctVerdicts: Int, totalEmails: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableStateOf(InboxStage.BRIEFING) }
    var emailIndex by remember { mutableIntStateOf(0) }

    // Inspected artifact ids, keyed globally — survives moving between emails.
    val inspected = remember { mutableStateMapOf<String, Boolean>() }
    val results = remember { mutableStateListOf<TriageResult>() }
    var lastVerdictWasPhishing by remember { mutableStateOf(false) }

    val emails = simulation.emails
    val total = emails.size

    fun commitVerdict(reportedAsPhishing: Boolean) {
        val email = emails[emailIndex]
        val found = email.artifacts.count { it.isRedFlag && inspected[it.id] == true }
        results.add(
            TriageResult(
                emailId = email.id,
                verdictCorrect = reportedAsPhishing == email.isPhishing,
                flagsFound = found,
                flagsTotal = email.redFlagCount
            )
        )
        lastVerdictWasPhishing = reportedAsPhishing
        stage = InboxStage.DEBRIEF
    }

    Box(modifier = modifier.fillMaxSize().background(AppNavy)) {
        when (stage) {
            InboxStage.BRIEFING -> InboxBriefing(
                simulation = simulation,
                xpReward = xpReward,
                onExit = onExit,
                onBegin = { stage = InboxStage.READING }
            )

            InboxStage.READING -> EmailWorkspace(
                email = emails[emailIndex],
                index = emailIndex,
                total = total,
                inspected = inspected,
                onInspect = { id -> inspected[id] = true },
                onVerdict = { commitVerdict(it) },
                onExit = onExit
            )

            InboxStage.DEBRIEF -> EmailDebrief(
                email = emails[emailIndex],
                result = results.last(),
                reportedAsPhishing = lastVerdictWasPhishing,
                inspected = inspected,
                isLast = emailIndex == total - 1,
                onNext = {
                    if (emailIndex == total - 1) {
                        stage = InboxStage.RESULT
                    } else {
                        emailIndex++
                        stage = InboxStage.READING
                    }
                }
            )

            InboxStage.RESULT -> {
                val correct = results.count { it.verdictCorrect }
                val flagsFound = results.sumOf { it.flagsFound }
                val flagsTotal = results.sumOf { it.flagsTotal }
                InboxResult(
                    correctVerdicts = correct,
                    totalEmails = total,
                    flagsFound = flagsFound,
                    flagsTotal = flagsTotal,
                    xpEarned = scoreXp(xpReward, correct, total, flagsFound, flagsTotal),
                    onFinish = {
                        onComplete(
                            scoreXp(xpReward, correct, total, flagsFound, flagsTotal),
                            correct,
                            total
                        )
                    }
                )
            }
        }
    }
}

/**
 * Verdicts carry most of the weight (70%); evidence gathered carries the rest (30%).
 * Guessing correctly without inspecting anything is possible but scores poorly —
 * which is the behaviour we want to discourage.
 */
private fun scoreXp(
    xpReward: Int,
    correct: Int,
    total: Int,
    flagsFound: Int,
    flagsTotal: Int
): Int {
    if (total == 0) return 0
    val verdictShare = 0.7 * correct / total
    val evidenceShare = if (flagsTotal == 0) 0.3 else 0.3 * flagsFound / flagsTotal
    return ((verdictShare + evidenceShare) * xpReward).toInt()
}

// ===========================================================================
// 5. BRIEFING
// ===========================================================================

@Composable
private fun InboxBriefing(
    simulation: InboxSimulation,
    xpReward: Int,
    onExit: () -> Unit,
    onBegin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onExit) {
            Icon(Icons.Filled.Close, contentDescription = "Exit simulation", tint = AppGray)
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(84.dp).background(AppBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Email, contentDescription = null, tint = AppWhite,
                modifier = Modifier.size(38.dp))
        }

        Spacer(Modifier.height(20.dp))
        Text("INTERACTIVE SIMULATION", color = AppCyan, fontSize = 12.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        Text(simulation.title, color = AppWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(simulation.briefing, color = AppGray, fontSize = 15.sp, lineHeight = 22.sp)

        Spacer(Modifier.height(20.dp))
        Row {
            InfoChip("${simulation.emails.size} messages")
            Spacer(Modifier.width(10.dp))
            InfoChip("up to +$xpReward XP")
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onBegin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
        ) {
            Text("OPEN INBOX", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ===========================================================================
// 6. EMAIL WORKSPACE — the actual simulated client
// ===========================================================================

@Composable
private fun EmailWorkspace(
    email: SimEmail,
    index: Int,
    total: Int,
    inspected: Map<String, Boolean>,
    onInspect: (String) -> Unit,
    onVerdict: (Boolean) -> Unit,
    onExit: () -> Unit
) {
    val inspectedHere = email.artifacts.count { inspected[it.id] == true }
    val progress by animateFloatAsState(
        targetValue = index.toFloat() / total,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "inboxProgress"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit simulation", tint = AppGray)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(8.dp),
                color = AppCyan,
                trackColor = AppCard
            )
            Spacer(Modifier.width(12.dp))
            Text("${index + 1}/$total", color = AppGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
        }

        // Inspection counter — tells the student investigation is the task
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = AppCyan,
                modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Inspected $inspectedHere of ${email.artifacts.size} elements",
                color = AppGray, fontSize = 12.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- Simulated mail client ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppCard, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(email.subject, color = AppWhite, fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, lineHeight = 23.sp)

                Spacer(Modifier.height(12.dp))

                // Sender row — inspectable
                email.artifacts.firstOrNull { it.kind == ArtifactKind.SENDER }?.let { art ->
                    ArtifactRow(
                        artifact = art,
                        isInspected = inspected[art.id] == true,
                        onInspect = { onInspect(art.id) },
                        leading = {
                            Box(
                                modifier = Modifier.size(36.dp)
                                    .background(AppBlue.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    email.senderDisplay.first().uppercase(),
                                    color = AppWhite, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(email.receivedAt, color = AppGray.copy(alpha = 0.7f), fontSize = 11.sp)

                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(AppGray.copy(alpha = 0.15f)))
                Spacer(Modifier.height(16.dp))

                // Body, with the greeting line swapped for an inspectable version
                val greeting = email.artifacts.firstOrNull { it.kind == ArtifactKind.GREETING }
                email.bodyParagraphs.forEachIndexed { i, para ->
                    if (i == 0 && greeting != null) {
                        ArtifactRow(
                            artifact = greeting,
                            isInspected = inspected[greeting.id] == true,
                            onInspect = { onInspect(greeting.id) }
                        )
                    } else {
                        Text(para, color = AppWhite.copy(alpha = 0.9f), fontSize = 14.sp,
                            lineHeight = 22.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Remaining inspectable artifacts, rendered as client affordances
                email.artifacts
                    .filter { it.kind !in setOf(ArtifactKind.SENDER, ArtifactKind.GREETING) }
                    .forEach { art ->
                        ArtifactRow(
                            artifact = art,
                            isInspected = inspected[art.id] == true,
                            onInspect = { onInspect(art.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Tap any element above to inspect it. When you're ready, make your call:",
                color = AppGray, fontSize = 13.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onVerdict(true) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlagRed.copy(alpha = 0.18f),
                        contentColor = FlagRed
                    )
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("REPORT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { onVerdict(false) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OkGreen.copy(alpha = 0.18f),
                        contentColor = OkGreen
                    )
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("MARK SAFE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * One inspectable element. Before inspection it shows only the surface value
 * with a magnifier affordance; tapping reveals the hidden value and a verdict
 * badge. This reveal is the core interaction of the simulation.
 */
@Composable
private fun ArtifactRow(
    artifact: Artifact,
    isInspected: Boolean,
    onInspect: () -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    val accent = when {
        !isInspected -> AppGray.copy(alpha = 0.3f)
        artifact.isRedFlag -> FlagRed
        else -> OkGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isInspected) accent.copy(alpha = 0.07f) else AppNavy.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, accent.copy(alpha = if (isInspected) 0.5f else 0.35f), RoundedCornerShape(12.dp))
            .clickable(enabled = !isInspected, onClick = onInspect)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kindLabel(artifact.kind),
                    color = AppCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(artifact.surface, color = AppWhite, fontSize = 14.sp)
            }

            Spacer(Modifier.width(8.dp))

            if (!isInspected) {
                Icon(Icons.Filled.Search, contentDescription = "Inspect", tint = AppCyan,
                    modifier = Modifier.size(18.dp))
            } else {
                Icon(
                    imageVector = if (artifact.isRedFlag) Icons.Filled.Warning else Icons.Filled.Check,
                    contentDescription = if (artifact.isRedFlag) "Red flag" else "Checks out",
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isInspected,
            enter = fadeIn(tween(200)) + expandVertically(tween(200))
        ) {
            Column {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppNavy, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = artifact.hidden,
                        color = accent,
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(artifact.explanation, color = AppGray, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

private fun kindLabel(kind: ArtifactKind): String = when (kind) {
    ArtifactKind.SENDER -> "SENDER"
    ArtifactKind.LINK -> "LINK"
    ArtifactKind.ATTACHMENT -> "ATTACHMENT"
    ArtifactKind.GREETING -> "GREETING"
    ArtifactKind.TONE -> "LANGUAGE"
    ArtifactKind.REPLY_TO -> "HEADER"
}

// ===========================================================================
// 7. DEBRIEF
// ===========================================================================

@Composable
private fun EmailDebrief(
    email: SimEmail,
    result: TriageResult,
    reportedAsPhishing: Boolean,
    inspected: Map<String, Boolean>,
    isLast: Boolean,
    onNext: () -> Unit
) {
    val accent = if (result.verdictCorrect) OkGreen else FlagRed

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.verdictCorrect) Icons.Filled.Check else Icons.Filled.Warning,
                    contentDescription = null, tint = accent, modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (result.verdictCorrect) "Correct call" else "Wrong call",
                    color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = buildVerdictLine(email.isPhishing, reportedAsPhishing),
                color = AppWhite, fontSize = 14.sp, lineHeight = 21.sp
            )

            Spacer(Modifier.height(16.dp))

            // Evidence scoreboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppCard, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("EVIDENCE GATHERED", color = AppCyan, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (result.flagsTotal == 0) {
                            "This message had no red flags to find."
                        } else {
                            "You found ${result.flagsFound} of ${result.flagsTotal} red flags before deciding."
                        },
                        color = AppWhite, fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Every artifact, including ones never inspected
            email.artifacts.forEach { art ->
                val wasInspected = inspected[art.id] == true
                val rowAccent = if (art.isRedFlag) FlagRed else OkGreen

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (wasInspected) AppCard else AppCard.copy(alpha = 0.45f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (art.isRedFlag) Icons.Filled.Warning else Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (wasInspected) rowAccent else rowAccent.copy(alpha = 0.4f),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(kindLabel(art.kind), color = AppGray, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold)
                        Text(
                            art.hidden,
                            color = if (wasInspected) AppWhite else AppGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (!wasInspected) {
                        Spacer(Modifier.width(8.dp))
                        Text("MISSED", color = AppGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBlue.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .border(1.dp, AppBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(email.debrief, color = AppWhite, fontSize = 13.sp, lineHeight = 20.sp)
            }

            Spacer(Modifier.height(20.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().background(AppCard).padding(16.dp)) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppCyan, contentColor = AppNavy)
            ) {
                Text(
                    text = if (isLast) "SEE RESULTS" else "NEXT MESSAGE",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun buildVerdictLine(wasPhishing: Boolean, reported: Boolean): String = when {
    wasPhishing && reported ->
        "This message was hostile, and you reported it. Nothing was compromised."
    wasPhishing && !reported ->
        "This message was hostile and you let it through. In a real inbox, that's the " +
                "moment credentials or a device get lost."
    !wasPhishing && reported ->
        "This message was genuine. Over-reporting buries the real threats and means you " +
                "miss legitimate deadlines."
    else ->
        "This message was genuine, and you correctly let it through."
}

// ===========================================================================
// 8. RESULT
// ===========================================================================

@Composable
private fun InboxResult(
    correctVerdicts: Int,
    totalEmails: Int,
    flagsFound: Int,
    flagsTotal: Int,
    xpEarned: Int,
    onFinish: () -> Unit
) {
    val passed = correctVerdicts == totalEmails
    val accent = if (passed) OkGreen else FlagRed

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
            text = if (passed) "Inbox secured" else "Inbox triaged",
            color = AppWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$correctVerdicts of $totalEmails messages called correctly",
            color = AppGray, fontSize = 15.sp
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            ScorePanel("VERDICTS", "$correctVerdicts/$totalEmails")
            Spacer(Modifier.width(12.dp))
            ScorePanel("EVIDENCE", "$flagsFound/$flagsTotal")
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .background(AppCard, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = AppCyan,
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("+$xpEarned XP", color = AppWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
private fun ScorePanel(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(AppCard, RoundedCornerShape(14.dp))
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = AppCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = AppWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .background(AppCard, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, color = AppWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
