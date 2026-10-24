package com.cyberity.cvsu

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal data class UpdateUiState(val update: AvailableUpdate? = null, val visible: Boolean = false,
    val checking: Boolean = false, val downloading: Boolean = false, val progress: Float = 0f,
    val file: File? = null, val message: String? = null)

internal object UpdateController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutable = MutableStateFlow(UpdateUiState())
    val state = mutable.asStateFlow()
    private var transfer: Job? = null
    private var manualRequested = false

    fun check(context: Context, manual: Boolean = true) {
        val app = context.applicationContext
        if (state.value.downloading) return
        if (state.value.checking) { manualRequested = manualRequested || manual; return }
        val prefs = UpdateNotifications.prefs(app)
        if (!manual && (!UpdateNotifications.enabled(app) ||
                System.currentTimeMillis() - prefs.getLong("checked", 0) in 0 until 6 * 60 * 60 * 1000L)) return
        mutable.update { it.copy(checking = true, message = null) }
        manualRequested = manual
        scope.launch {
            try {
                val update = withContext(Dispatchers.IO) { UpdateDownloads.latest() }
                val installed = app.packageManager.getPackageInfo(app.packageName, 0).versionName.orEmpty()
                prefs.edit().putLong("checked", System.currentTimeMillis()).apply()
                if (AppVersion.isNewer(update.version, installed)) {
                    if (!manualRequested) UpdateNotifications.notify(app, update)
                    val show = manualRequested || prefs.getString("dismissed", null) != update.version
                    mutable.update { old -> old.copy(update = update, visible = show,
                        file = old.file.takeIf { old.update == update && it?.exists() == true }) }
                } else {
                    UpdateNotifications.clear(app)
                    mutable.value = UpdateUiState(message = if (manualRequested) "You’re up to date." else null)
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { if (manualRequested) mutable.update { it.copy(message = "Couldn't check for updates. Check your connection and try again.") } }
            finally { manualRequested = false; mutable.update { it.copy(checking = false) } }
        }
    }

    fun dismiss(context: Context) {
        state.value.update?.let { UpdateNotifications.prefs(context).edit().putString("dismissed", it.version).apply() }
        mutable.update { it.copy(visible = false) }
    }

    fun download(context: Context) {
        if (state.value.downloading) return
        val update = state.value.update ?: return
        mutable.update { it.copy(downloading = true, progress = 0f, message = null, file = null) }
        transfer = scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    UpdateDownloads.download(context.applicationContext, update) { progress -> mutable.update { it.copy(progress = progress) } }
                }
                mutable.update { it.copy(file = file, message = "Download verified. Ready to install.") }
            } catch (e: CancellationException) { mutable.update { it.copy(message = "Download canceled.") }; throw e }
            catch (_: Exception) { mutable.update { it.copy(message = "Download couldn't be verified. Check your connection and try again.") } }
            finally { mutable.update { it.copy(downloading = false) } }
        }
    }

    fun cancel() { transfer?.cancel() }
    fun message(text: String) { mutable.update { it.copy(message = text) } }
    suspend fun verifiedInstaller(context: Context): android.content.Intent {
        val snapshot = state.value
        val file = requireNotNull(snapshot.file)
        val update = requireNotNull(snapshot.update)
        try { withContext(Dispatchers.IO) { UpdateDownloads.verify(context, file, update) } }
        catch (e: Exception) { file.delete(); mutable.update { it.copy(file = null) }; throw e }
        return UpdateDownloads.installerIntent(context, file)
    }
}
