package com.cyberity.cvsu

internal object AuthCredentialErrors {
    const val message = "Invalid email or password. Please try again."
    fun messageFor(code: String): String? = if (code in setOf(
        "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND",
        "ERROR_USER_DISABLED", "ERROR_INVALID_EMAIL")) message else null
}
