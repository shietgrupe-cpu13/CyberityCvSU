package com.cyberity.cvsu

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CertificateDetails(
    val name: String, val studentId: String, val year: String,
    val issuedOn: String, val number: String, val previewOnly: Boolean
)

@Composable
fun CompletionCertificateScreen() {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()
    var profile by remember(uid) { mutableStateOf<UserProfile?>(null) }
    var completed by remember(uid) { mutableStateOf<Set<Int>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var exporting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<CertificateDetails?>(null) }
    var developerMode by remember(uid) { mutableStateOf(false) }
    DisposableEffect(uid) {
        val listener = DeveloperModeRepository.listen(uid) { developerMode = it }
        onDispose { listener?.remove() }
    }
    val required = remember { CertificatePolicy.requiredLevels(sampleLearningUnits()) }
    val download = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val details = pending
        pending = null
        if (uri != null && details != null) scope.launch {
            exporting = true
            try {
                check(FirebaseAuth.getInstance().currentUser?.uid == uid) { "Your account changed. Try again." }
                if (details.previewOnly) {
                    check(uid != null && DeveloperModeRepository.confirm(uid).privacyAwait()) {
                        "Developer access is no longer available."
                    }
                }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val pdf = PdfDocument()
                        try {
                            val page = pdf.startPage(PdfDocument.PageInfo.Builder(842, 595, 1).create())
                            drawCertificate(page.canvas, details)
                            pdf.finishPage(page)
                            pdf.writeTo(output)
                        } finally { pdf.close() }
                    } ?: error("Couldn't open the selected location.")
                }
                message = if (details.previewOnly) "Your preview PDF has been saved." else "Your certificate has been saved."
            } catch (failure: Exception) {
                if (failure is kotlinx.coroutines.CancellationException) throw failure
                message = if (details.previewOnly) "Couldn't save the sample. Check your developer access and connection, then try again."
                    else "Couldn't save the certificate. Try another location."
            } finally { exporting = false }
        }
    }
    LaunchedEffect(uid, refresh) {
        loading = true; error = null
        if (uid == null) { loading = false; error = "Please sign in again."; return@LaunchedEffect }
        try {
            profile = UserProfileRepository.loadConfirmed(uid).privacyAwait()
            check(profile != null) { "Student information is unavailable." }
            completed = ProgressRepository.load(uid).privacyAwait().completed
        }
        catch (failure: Exception) {
            if (failure is kotlinx.coroutines.CancellationException) throw failure
            error = "Couldn't verify course completion. Check your connection and try again."
        } finally { loading = false }
    }
    val eligible = !loading && error == null && CertificatePolicy.canDownload(profile, completed, required)
    val downloadKind = if (loading || error != null) CertificatePolicy.DownloadKind.LOCKED
        else CertificatePolicy.downloadKind(profile, completed, required, developerMode)
    val details = profile?.let {
        CertificateDetails(it.certificateName.ifBlank { "Add your full name in Settings" }, it.studentId, YearLevels.label(it.yearLevel),
            SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(Date()),
            CertificatePolicy.certificateNumber(uid.orEmpty()), !eligible)
    }
    SettingsNote("Your full name, student ID and year level are filled in from Student information. Complete every required course level to download your certificate.")
    if (loading) CircularProgressIndicator(color = AppCyan)
    SettingsCard {
        Text("${completed.intersect(required).size} of ${required.size} required levels completed", color = AppWhite)
        if (downloadKind == CertificatePolicy.DownloadKind.PREVIEW)
            Text("Developer access: you can download a sample marked PREVIEW. It does not certify course completion.", color = AppCyan)
        else if (profile != null && StudentIdRules.validateFullName(profile!!.certificateName) != null)
            Text("Add your full name in Settings > Student information before downloading.", color = AppCyan)
        else if (profile != null && !YearLevels.isValid(profile?.yearLevel))
            Text("Add your year level in Settings > Student information before downloading.", color = AppCyan)
        else Text(if (eligible) "Your certificate is ready." else "Preview your certificate below. Download unlocks when the course is complete.", color = AppGray)
    }
    details?.let { current ->
        val preview = remember(current) {
            Bitmap.createBitmap(1263, 893, Bitmap.Config.ARGB_8888).also { bitmap ->
                val canvas = Canvas(bitmap)
                canvas.scale(1.5f, 1.5f)
                drawCertificate(canvas, current)
            }.asImageBitmap()
        }
        Image(preview, contentDescription = "Certificate for ${current.name}, student ID ${current.studentId}, ${current.year}${if (current.previewOnly) ", preview only" else ""}",
            modifier = Modifier.fillMaxWidth().aspectRatio(842f / 595f))
        Button(onClick = {
            pending = current
            download.launch("Cyberity-${if (current.previewOnly) "PREVIEW-" else ""}${current.number}.pdf")
        }, enabled = downloadKind != CertificatePolicy.DownloadKind.LOCKED && !exporting && pending == null,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (exporting) "Saving..." else if (downloadKind == CertificatePolicy.DownloadKind.PREVIEW) "Download sample PDF" else "Download certificate")
        }
    }
    error?.let { Text(it, color = AppDanger) }
    message?.let { Text(it, color = AppCyan) }
    TextButton(onClick = { refresh++ }, enabled = !loading && !exporting) { Text("Refresh certificate details") }
}

private fun drawCertificate(canvas: Canvas, details: CertificateDetails) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val green = Color.rgb(29, 89, 69)
    canvas.drawColor(Color.WHITE)
    paint.style = Paint.Style.STROKE; paint.color = green; paint.strokeWidth = 2f
    canvas.drawRect(22f, 22f, 820f, 573f, paint)
    paint.strokeWidth = .6f; canvas.drawRect(28f, 28f, 814f, 567f, paint)
    if (details.previewOnly) {
        canvas.save()
        canvas.rotate(-22f, 421f, 297f)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(26, 29, 89, 69)
        paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        paint.textSize = 100f; paint.textAlign = Paint.Align.CENTER
        canvas.drawText("PREVIEW", 421f, 327f, paint)
        canvas.restore()
    }
    fun line(text: String, y: Float, size: Float = 12f, bold: Boolean = false, serif: Boolean = false,
             color: Int = Color.rgb(38, 59, 53), x: Float = 421f, maxWidth: Float = 710f) {
        paint.style = Paint.Style.FILL; paint.textAlign = Paint.Align.CENTER; paint.color = color
        paint.typeface = Typeface.create(if (serif) "serif" else "sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        paint.textSize = size
        if (paint.measureText(text) > maxWidth) paint.textSize = size * maxWidth / paint.measureText(text)
        canvas.drawText(text, x, y, paint)
    }
    line("CYBERITY", 63f, 19f, true, color = Color.BLACK)
    line("CYBERSECURITY LEARNING PROGRAM", 81f, 10f, color = Color.BLACK)
    line("Certificate of Completion", 136f, 34f, serif = true, color = Color.BLACK)
    line("This certificate is proudly presented to", 169f)
    line(details.name, 211f, 29f, true, true, green)
    line("Student ID: ${details.studentId}  |  ${details.year}", 235f, 12f)
    line("in recognition of successfully completing all required lessons, interactive", 270f, 12.5f)
    line("simulations, and learning activities in", 288f, 12.5f)
    line("Cyberity Cybersecurity Awareness Course", 319f, 17f, true, color = Color.BLACK)
    line("Your dedication to learning and practicing safer digital habits is sincerely appreciated.", 351f, 11.5f)
    line("Thank you for completing Cyberity and helping build a safer digital community.", 369f, 11.5f)
    line("Issued on ${details.issuedOn}  |  Certificate No. ${details.number}", 403f, 10.5f)
    CertificateBranding.signatoryNames.forEachIndexed { index, name ->
        val x = 174f + index * 247f
        line("Signature to be added", 458f, 8.5f, color = Color.GRAY, x = x, maxWidth = 190f)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = .6f; paint.color = green
        canvas.drawLine(x - 87, 468f, x + 87, 468f, paint)
        if (name.isNotBlank()) line(name, 488f, 10f, true, x = x, maxWidth = 190f)
        line(CertificateBranding.signatoryTitle, 506f, 10f, x = x, maxWidth = 190f)
    }
    if (details.previewOnly) line("PREVIEW ONLY - NOT PROOF OF COURSE COMPLETION", 548f, 9f, true, color = green)
}
