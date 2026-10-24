package com.cyberity.cvsu

import android.app.job.JobScheduler
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class ProtectedUpdateTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun installerCanShareOnlyThePrivateUpdateDirectory() {
        val directory = File(context.cacheDir, "updates").apply { mkdirs() }
        val sample = File(directory, "provider-test.apk").apply { writeBytes(byteArrayOf(1)) }
        val outside = File(context.cacheDir, "outside-update-test.apk").apply { writeBytes(byteArrayOf(1)) }
        try {
            val intent = UpdateDownloads.installerIntent(context, sample)
            assertEquals("content", intent.data!!.scheme)
            assertEquals(context.packageName + ".updates", intent.data!!.authority)
            assertEquals("application/vnd.android.package-archive", intent.type)
            assertTrue(intent.flags and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            assertTrue(runCatching { UpdateDownloads.installerIntent(context, outside) }.isFailure)
        } finally { sample.delete(); outside.delete() }
    }

    @Test fun rejectsTamperedDownloadAndCurrentVersion() {
        val installed = context.packageManager.getPackageInfo(context.packageName, 0)
        val apk = File(installed.applicationInfo!!.sourceDir)
        val hash = MessageDigest.getInstance("SHA-256")
        apk.inputStream().use { stream ->
            val bytes = ByteArray(65536)
            while (true) { val count = stream.read(bytes); if (count < 0) break; hash.update(bytes, 0, count) }
        }
        val digest = hash.digest().joinToString("") { "%02x".format(it) }
        val update = AvailableUpdate("v" + installed.versionName, "", digest, apk.length())
        // Real package/signing metadata must still reject reinstalling the current version.
        assertTrue(runCatching { UpdateDownloads.verify(context, apk, update) }.isFailure)
        assertTrue(runCatching { UpdateDownloads.verify(context, apk, update.copy(sha256 = "0".repeat(64))) }.isFailure)
    }

    @Test fun periodicChecksArePersistedAndCanBeTurnedOff() {
        val original = UpdateNotifications.enabled(context)
        try {
            UpdateNotifications.configure(context, true)
            val job = context.getSystemService(JobScheduler::class.java).getPendingJob(4602)
            assertNotNull(job)
            assertTrue(job!!.isPersisted)
            assertTrue(job.isPeriodic)
            UpdateNotifications.configure(context, false)
            assertNull(context.getSystemService(JobScheduler::class.java).getPendingJob(4602))
        } finally { UpdateNotifications.configure(context, original) }
    }

    @Test fun latestPublicReleaseHasTrustedAddressAndChecksum() = runBlocking {
        val release = UpdateDownloads.latest()
        assertTrue(UpdatePolicy.assetUrl(release.downloadUrl, release.version))
        assertNotNull(UpdatePolicy.digest("sha256:" + release.sha256))
        assertTrue(release.size in 1..UpdatePolicy.MAX_APK_BYTES)
    }
}
