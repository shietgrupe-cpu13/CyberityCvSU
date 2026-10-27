package com.cyberity.cvsu

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

internal data class StudentIdentity(val fullName: String, val locked: Boolean)

internal object StudentNameLock {
    const val FIRST_LEVEL = 101
    private val db get() = FirebaseFirestore.getInstance()

    fun identity(profile: DocumentSnapshot, progress: DocumentSnapshot): StudentIdentity = StudentIdentity(
        profile.getString("fullName").orEmpty(),
        profile.get("fullNameLockedAt") != null ||
            progress.get("completedLevels").let { it is List<*> && it.any { id -> (id as? Number)?.toInt() == FIRST_LEVEL } })

    fun load(uid: String): Task<StudentIdentity> {
        val user = db.collection("users").document(uid).get(Source.SERVER)
        val progress = db.collection("userProgress").document(uid).get(Source.SERVER)
        return Tasks.whenAll(user, progress).continueWith {
            it.result
            check(user.result.exists()) { "Your student information is unavailable." }
            identity(user.result, progress.result)
        }
    }

    /** The transaction serializes name edits with the first-level start on every device. */
    fun confirmStart(uid: String, expectedName: String): Task<Void> {
        val user = db.collection("users").document(uid)
        val progress = db.collection("userProgress").document(uid)
        return db.runTransaction { tx ->
            val profile = tx.get(user)
            val state = identity(profile, tx.get(progress))
            check(profile.exists() && StudentIdRules.validateFullName(state.fullName) == null) {
                "Add your full name in Settings > Student Information before starting Unit 1, Level 1."
            }
            check(state.fullName == expectedName) { "Your name changed on another device. Review it again before starting." }
            if (!state.locked) tx.update(user, "fullNameLockedAt", FieldValue.serverTimestamp())
            null
        }
    }
}

@androidx.compose.runtime.Composable
internal fun ConfirmStudentNameDialog(name: String, busy: Boolean, error: String?, onStart: () -> Unit, onCancel: () -> Unit) {
    androidx.compose.material3.AlertDialog(onDismissRequest = { if (!busy) onCancel() },
        title = { androidx.compose.material3.Text("Confirm Your Full Name") },
        text = {
            androidx.compose.foundation.layout.Column {
                androidx.compose.material3.Text(name)
                androidx.compose.material3.Text("Starting Unit 1, Level 1 will lock this full name for your certificate. You won't be able to edit it afterward. To correct it first, choose Not Yet and open Settings > Student Information.")
                error?.let { androidx.compose.material3.Text(it, color = AppDanger) }
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = onStart, enabled = !busy) {
            androidx.compose.material3.Text(if (busy) "Starting…" else "Confirm and Start")
        } },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onCancel, enabled = !busy) {
            androidx.compose.material3.Text("Not Yet")
        } })
}
