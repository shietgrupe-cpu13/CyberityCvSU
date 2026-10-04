package com.cyberity.cvsu

import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.delay

/** Device-wide, so changing the email or revisiting the form cannot reset it.
 * No email, password, OTP, or Firebase token is stored here.
 */
internal object AuthAttemptGuard {
    const val PREFS = "auth_attempt_guard"
    private val guards = mutableMapOf<Boolean, AuthThrottle>()

    @Synchronized fun get(context: Context, passwordReset: Boolean = false): AuthThrottle =
        guards.getOrPut(passwordReset) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val key = if (passwordReset) "reset_" else "signin_"
            val state = AuthThrottle.State(
                prefs.getInt(key + "failures", 0), prefs.getInt(key + "penalty", 0),
                prefs.getLong(key + "blocked_until", 0), prefs.getLong(key + "last_failure", 0),
                prefs.getLong(key + "next_request", 0)
            )
            AuthThrottle(state, System::currentTimeMillis, save = { value ->
                prefs.edit {
                    putInt(key + "failures", value.failures)
                    putInt(key + "penalty", value.penalty)
                    putLong(key + "blocked_until", value.blockedUntil)
                    putLong(key + "last_failure", value.lastFailure)
                    putLong(key + "next_request", value.nextRequest)
                }
            }, requestIntervalMillis = if (passwordReset) 60_000L else 1_000L)
        }
}

@Composable
internal fun rememberRetrySeconds(guard: AuthThrottle): Long {
    var seconds by remember(guard) { mutableLongStateOf(guard.retryAfterSeconds()) }
    LaunchedEffect(guard) {
        while (true) {
            seconds = guard.retryAfterSeconds()
            // Updates security countdown feedback only; never holds startup.
            delay(1_000L)
        }
    }
    return seconds
}

internal fun AuthThrottle.handleFailure(error: Exception, secondFactor: Boolean = false): String = when {
    error is FirebaseTooManyRequestsException ||
        (error is FirebaseAuthException && error.errorCode == "ERROR_TOO_MANY_REQUESTS") -> {
        serverThrottled()
        "Too many attempts. Please wait before trying again."
    }
    error is FirebaseNetworkException -> {
        finished()
        "Couldn't connect. Check your internet connection and try again."
    }
    error is FirebaseAuthInvalidCredentialsException || error is FirebaseAuthInvalidUserException -> {
        rejected()
        if (secondFactor) "That code could not be verified. Try the current code."
        else "Unable to sign in with those credentials. Check your email and password."
    }
    else -> {
        finished()
        if (secondFactor) "Couldn't verify the code. Try again, or cancel and sign in again."
        else "Couldn't sign in. Please try again later."
    }
}
