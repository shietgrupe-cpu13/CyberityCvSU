package com.cyberity.cvsu

import com.google.firebase.firestore.FirebaseFirestoreException

internal fun completionSaveMessage(error: Throwable): String {
    val failure = generateSequence(error) { it.cause }.take(10)
        .filterIsInstance<FirebaseFirestoreException>().firstOrNull()
    val reason = when (failure?.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "The server refused this save (PERMISSION_DENIED). Reopen the app and retry. If it continues, report this code to your app administrator."
        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            "Your sign-in needs to be refreshed. Sign in again with the same account, then retry."
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
            "The server couldn't be reached. Check your connection and retry."
        else -> "Your result hasn't been confirmed yet. Retry saving. If it continues, reopen the app and retry."
    }
    return "$reason Your result is kept on this device for retry. You won't receive duplicate XP."
}
