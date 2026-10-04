package com.cyberity.cvsu

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
import org.json.JSONArray
import org.json.JSONObject

internal object PrivacyAccountData {
    private val db get() = FirebaseFirestore.getInstance()

    /** Always uses server reads: an incomplete cached copy must not be labeled a complete export. */
    suspend fun export(uid: String): String {
        val user = FirebaseAuth.getInstance().currentUser
        check(user?.uid == uid) { "Please sign in again." }
        val exportedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date())
        val result = JSONObject().put("formatVersion", 1).put("exportedAt", exportedAt)
            .put("account", JSONObject().put("uid", uid).put("email", user?.email ?: JSONObject.NULL)
                .put("emailVerified", user?.isEmailVerified).put("createdAtMillis", user?.metadata?.creationTimestamp)
                .put("lastSignInAtMillis", user?.metadata?.lastSignInTimestamp))
        val records = JSONObject()
        for (collection in listOf("users", "userProgress", "leaderboard", "accountSecuritySpark", "privacyPreferences")) {
            val document = db.collection(collection).document(uid).get(Source.SERVER).privacyAwait()
            records.put("$collection/$uid", jsonValue(document.data))
        }
        val claims = db.collection("studentIds").whereEqualTo("uid", uid).get(Source.SERVER).privacyAwait()
        claims.documents.forEach { records.put(it.reference.path, jsonValue(it.data)) }
        val sessions = db.collection("accountSecuritySpark").document(uid).collection("sessions")
            .get(Source.SERVER).privacyAwait()
        sessions.documents.forEach { records.put(it.reference.path, jsonValue(it.data)) }
        check(FirebaseAuth.getInstance().currentUser?.uid == uid) { "Your account changed. Try again." }
        return result.put("records", records).toString(2)
    }

    /** Mark first, drain owned records in bounded batches, delete Auth last. Retry repeats safely. */
    suspend fun delete(uid: String, beforeAuthDeletion: () -> Unit = {}) {
        val user = FirebaseAuth.getInstance().currentUser
        check(user?.uid == uid) { "Please sign in again." }
        val token = user!!.getIdToken(true).privacyAwait()
        val authTime = (token.claims["auth_time"] as? Number)?.toLong()
            ?: error("Verify your account again.")
        PrivacyPreferencesRepository.activeDeletionUid = uid
        try {
            db.collection("privacyPreferences").document(uid).set(mapOf(
                "leaderboardVisible" to false, "deleting" to true, "deletionAuthTime" to authTime
            )).privacyAwait()
            // Marker blocks concurrent normal app writes, including from older devices.
            drain(db.collection("accountSecuritySpark").document(uid).collection("sessions"))
            drain(db.collection("studentIds").whereEqualTo("uid", uid))
            val batch = db.batch()
            for (collection in listOf("users", "userProgress", "leaderboard"))
                batch.delete(db.collection(collection).document(uid))
            batch.commit().privacyAwait()
            check(FirebaseAuth.getInstance().currentUser?.uid == uid) { "Please sign in to finish deletion." }
            // Auth deletion can immediately unmount this screen. Finish local cleanup first.
            beforeAuthDeletion()
            // Revocation and deletion tombstones intentionally remain, preventing stale token access.
            user.delete().privacyAwait()
        } finally {
            PrivacyPreferencesRepository.activeDeletionUid = null
        }
    }

    private suspend fun drain(query: com.google.firebase.firestore.Query) {
        while (true) {
            val documents = query.limit(400).get(Source.SERVER).privacyAwait().documents
            if (documents.isEmpty()) return
            val batch = db.batch()
            documents.forEach { batch.delete(it.reference) }
            batch.commit().privacyAwait()
        }
    }

    private fun jsonValue(value: Any?): Any = when (value) {
        null -> JSONObject.NULL
        is Timestamp -> JSONObject().put("seconds", value.seconds).put("nanoseconds", value.nanoseconds)
        is DocumentReference -> value.path
        is GeoPoint -> JSONObject().put("latitude", value.latitude).put("longitude", value.longitude)
        is Map<*, *> -> JSONObject().also { obj -> value.forEach { (key, item) -> obj.put(key.toString(), jsonValue(item)) } }
        is Iterable<*> -> JSONArray().also { array -> value.forEach { array.put(jsonValue(it)) } }
        is com.google.firebase.firestore.Blob -> android.util.Base64.encodeToString(value.toBytes(), android.util.Base64.NO_WRAP)
        else -> value
    }
}
