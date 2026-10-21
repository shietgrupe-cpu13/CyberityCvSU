package com.cyberity.cvsu

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object AppVersion {
    fun parts(value: String): List<Int>? {
        val text = value.removePrefix("v")
        if (!text.matches(Regex("[0-9]+([.][0-9]+)*"))) return null
        return text.split('.').map { it.toIntOrNull() ?: return null }
    }
    fun isNewer(latest: String, installed: String): Boolean {
        val a = requireNotNull(parts(latest))
        val b = requireNotNull(parts(installed))
        for (i in 0 until maxOf(a.size, b.size)) {
            val comparison = (a.getOrElse(i) { 0 }).compareTo(b.getOrElse(i) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }
}

private data class AvailableUpdate(val version: String, val downloadUrl: String)

@Composable
fun CheckForUpdates() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var available by remember { mutableStateOf<AvailableUpdate?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    Button(onClick = {
        checking = true; message = null; available = null
        scope.launch {
            try {
                val installed = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: error("Version unavailable")
                val update = withContext(Dispatchers.IO) {
                    val connection = URL("https://api.github.com/repos/shietgrupe-cpu13/CyberityCvSU/releases/latest").openConnection() as HttpURLConnection
                    try {
                        connection.connectTimeout = 10000; connection.readTimeout = 10000
                        connection.setRequestProperty("Accept", "application/vnd.github+json")
                        connection.setRequestProperty("User-Agent", "Cyberity-Android")
                        connection.useCaches = false
                        check(connection.responseCode == 200) { "Release unavailable" }
                        val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                        check(!release.optBoolean("draft") && !release.optBoolean("prerelease"))
                        val version = release.getString("tag_name")
                        val assets = release.getJSONArray("assets")
                        val asset = (0 until assets.length()).map { assets.getJSONObject(it) }
                            .first { it.getString("name") == "CyberityCvSU.apk" }
                        val url = asset.getString("browser_download_url")
                        check(url.startsWith("https://github.com/shietgrupe-cpu13/CyberityCvSU/releases/download/"))
                        AvailableUpdate(version, url)
                    } finally { connection.disconnect() }
                }
                if (AppVersion.isNewer(update.version, installed)) available = update
                else message = "You’re up to date."
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { message = "Couldn't check for updates. Check your connection and try again." }
            finally { checking = false }
        }
    }, enabled = !checking, modifier = Modifier.fillMaxWidth()) {
        Text(if (checking) "Checking for updates…" else "Check for updates")
    }
    message?.let { Text(it, color = AppCyan) }
    available?.let { update ->
        UpdatePrompt(update.version, onCancel = { available = null }, onDownload = {
            available = null
            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))) }
            catch (_: Exception) { message = "Couldn't open the download. Try again after enabling a browser." }
        })
    }
}

@Composable
internal fun UpdatePrompt(version: String, onDownload: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(onDismissRequest = onCancel,
        title = { Text("Update available") },
        text = { Text("Cyberity "+version+" is available. Would you like to download it? Your browser will open the APK download.") },
        confirmButton = { TextButton(onClick = onDownload) { Text("Download") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } })
}
