package com.cyberity.cvsu

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest

internal data class AvailableUpdate(val version: String, val downloadUrl: String, val sha256: String, val size: Long)

internal object UpdateDownloads {
    private const val LATEST = "https://api.github.com/repos/shietgrupe-cpu13/CyberityCvSU/releases/latest"

    fun latest(): AvailableUpdate {
        val connection = URL(LATEST).openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000; connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "Cyberity-Android")
            connection.useCaches = false
            check(connection.responseCode == 200) { "Release unavailable" }
            val data = connection.inputStream.use { it.readBytesBounded(256 * 1024) }
            val release = JSONObject(data.toString(Charsets.UTF_8))
            check(!release.optBoolean("draft") && !release.optBoolean("prerelease"))
            val version = release.getString("tag_name")
            check(AppVersion.parts(version) != null)
            val assets = release.getJSONArray("assets")
            val asset = (0 until assets.length()).map { assets.getJSONObject(it) }
                .single { it.getString("name") == "CyberityCvSU.apk" }
            val url = asset.getString("browser_download_url")
            check(UpdatePolicy.assetUrl(url, version))
            val digest = requireNotNull(UpdatePolicy.digest(asset.optString("digest")))
            val size = asset.getLong("size")
            check(size in 1..UpdatePolicy.MAX_APK_BYTES)
            return AvailableUpdate(version, url, digest, size)
        } finally { connection.disconnect() }
    }

    private fun java.io.InputStream.readBytesBounded(max: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = read(buffer)
            if (count == -1) break
            check(output.size() + count <= max)
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun connection(url: String): HttpURLConnection {
        var current = url
        repeat(6) {
            check(UpdatePolicy.downloadUrl(current)) { "Untrusted download address" }
            val c = URL(current).openConnection() as HttpURLConnection
            c.instanceFollowRedirects = false
            c.connectTimeout = 15_000; c.readTimeout = 20_000
            c.setRequestProperty("User-Agent", "Cyberity-Android")
            c.setRequestProperty("Accept-Encoding", "identity")
            try {
                when (c.responseCode) {
                    200 -> return c
                    301, 302, 303, 307, 308 -> {
                        current = URI(current).resolve(requireNotNull(c.getHeaderField("Location"))).toString()
                        c.disconnect()
                    }
                    else -> error("Download unavailable")
                }
            } catch (e: Exception) { c.disconnect(); throw e }
        }
        error("Too many redirects")
    }

    suspend fun download(context: Context, update: AvailableUpdate, progress: (Float) -> Unit): File {
        check(UpdatePolicy.assetUrl(update.downloadUrl, update.version))
        val directory = File(context.cacheDir, "updates").apply { mkdirs() }
        val partial = File(directory, "update.part")
        val ready = File(directory, "update.apk")
        ready.delete(); partial.delete()
        try {
            val c = connection(update.downloadUrl)
            try {
                check(c.contentLengthLong == -1L || c.contentLengthLong == update.size)
                var total = 0L
                c.inputStream.use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = input.read(buffer)
                            if (count == -1) break
                            total += count
                            check(total <= update.size && total <= UpdatePolicy.MAX_APK_BYTES)
                            output.write(buffer, 0, count)
                            progress(total.toFloat() / update.size)
                        }
                    }
                }
                check(total == update.size) { "Incomplete download" }
            } finally { c.disconnect() }
            verify(context, partial, update)
            check(partial.renameTo(ready))
            return ready
        } catch (e: Exception) { partial.delete(); ready.delete(); throw e }
    }

    @Suppress("DEPRECATION")
    fun verify(context: Context, file: File, update: AvailableUpdate) {
        check(file.length() == update.size)
        val hash = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val bytes = ByteArray(64 * 1024)
            while (true) { val count = input.read(bytes); if (count == -1) break; hash.update(bytes, 0, count) }
        }
        check(UpdatePolicy.matchesDigest(hash.digest(), update.sha256)) { "Download checksum did not match" }
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val pm = context.packageManager
        val installed = pm.getPackageInfo(context.packageName, flags)
        val candidate = requireNotNull(pm.getPackageArchiveInfo(file.absolutePath, flags))
        fun signers(info: PackageInfo): Set<String> {
            val certificates = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
            return certificates.orEmpty().map { signature ->
                MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).joinToString("") { "%02x".format(it) }
            }.toSet()
        }
        fun code(info: PackageInfo) = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
        check(UpdatePolicy.validPackage(context.packageName, candidate.packageName, code(installed), code(candidate),
            update.version, candidate.versionName, signers(installed), signers(candidate))) { "Update identity or signing certificate did not match" }
    }

    fun installerIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".updates", file)
        return Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { clipData = android.content.ClipData.newRawUri("Cyberity update", uri) }
    }
}
