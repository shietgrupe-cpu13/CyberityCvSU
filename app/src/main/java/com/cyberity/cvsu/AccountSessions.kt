package com.cyberity.cvsu

import android.content.Context
import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import java.util.UUID

internal data class AccountSession(val id: String, val device: String, val lastActive: Long, val current: Boolean)

/** Spark app access cutoff, enforced by Firestore rules. Does not revoke Auth tokens. */
internal object AccountSessions {
    private val db get() = FirebaseFirestore.getInstance()
    private class Revoked : Exception("Session expired")
    private const val HISTORY_MS = 90L * 24 * 60 * 60 * 1000

    fun id(context: Context): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        val prefs = context.getSharedPreferences("account_sessions", Context.MODE_PRIVATE)
        return prefs.getString(uid, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(uid, it).apply()
        }
    }

    fun clear(context: Context) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            context.getSharedPreferences("account_sessions", Context.MODE_PRIVATE).edit().remove(it).apply()
        }
    }

    fun end(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Best effort; local logout must also work offline.
        db.collection("accountSecuritySpark").document(uid).collection("sessions")
            .document(id(context)).update("signedOut", true)
        clear(context)
    }

    private fun checked(onSuccess: (String, Long) -> Unit, onError: (Exception) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: run { onError(Revoked()); return }
        user.getIdToken(false).addOnSuccessListener { token ->
            val authTime = (token.claims["auth_time"] as? Number)?.toLong()
            if (authTime == null) { onError(Revoked()); return@addOnSuccessListener }
            db.collection("accountSecuritySpark").document(user.uid).get(Source.SERVER)
                .addOnSuccessListener { metadata ->
                    val cutoff = metadata.getTimestamp("revokedAt")
                    if (FirebaseAuth.getInstance().currentUser?.uid != user.uid ||
                        (cutoff != null && Timestamp(authTime, 0) <= cutoff)) onError(Revoked())
                    else onSuccess(user.uid, authTime)
                }.addOnFailureListener(onError)
        }.addOnFailureListener(onError)
    }

    fun touch(context: Context, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        checked({ uid, authTime ->
            db.collection("accountSecuritySpark").document(uid).collection("sessions")
                .document(id(context)).set(mapOf(
                    "device" to "${Build.MANUFACTURER} ${Build.MODEL}".take(100),
                    "androidVersion" to Build.VERSION.RELEASE.take(30),
                    "authTime" to authTime, "lastActive" to FieldValue.serverTimestamp(),
                    "signedOut" to false
                )).addOnSuccessListener { onSuccess() }.addOnFailureListener(onError)
        }, onError)
    }

    fun list(context: Context, onResult: (List<AccountSession>) -> Unit, onError: (Exception) -> Unit) {
        val currentId = id(context)
        checked({ uid, _ ->
            val metadata = db.collection("accountSecuritySpark").document(uid)
            metadata.get(Source.SERVER).addOnSuccessListener { security ->
                val cutoff = security.getTimestamp("revokedAt")
                metadata.collection("sessions").orderBy("lastActive", Query.Direction.DESCENDING)
                    .limit(50).get(Source.SERVER).addOnSuccessListener { snapshot ->
                        onResult(snapshot.documents.mapNotNull { doc ->
                            val time = doc.getTimestamp("lastActive")?.toDate()?.time ?: return@mapNotNull null
                            val authTime = doc.getLong("authTime") ?: return@mapNotNull null
                            if (doc.getBoolean("signedOut") == true || time < System.currentTimeMillis() - HISTORY_MS ||
                                (cutoff != null && Timestamp(authTime, 0) <= cutoff)) return@mapNotNull null
                            AccountSession(doc.id, doc.getString("device") ?: "Android device", time, doc.id == currentId)
                        })
                    }.addOnFailureListener(onError)
            }.addOnFailureListener(onError)
        }, onError)
    }

    fun revokeAll(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        checked({ uid, _ ->
            db.collection("accountSecuritySpark").document(uid)
                .set(mapOf("revokedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                .addOnSuccessListener { onSuccess() }.addOnFailureListener(onError)
        }, onError)
    }

    fun isRevoked(error: Exception) = error is Revoked

    fun message(error: Exception): String = when {
        isRevoked(error) -> "Your session has expired. Please sign in again."
        error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Device management access was denied. Sign in again or check the Firestore security setup."
        else -> "Couldn't connect to device management. Check your connection and try again."
    }
}
