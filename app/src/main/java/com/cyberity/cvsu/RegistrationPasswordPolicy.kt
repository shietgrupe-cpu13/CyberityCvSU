package com.cyberity.cvsu

object RegistrationPasswordPolicy {
    const val guidance = "At least 8 characters with uppercase, lowercase, a number, and a symbol."
    fun validate(password: String): String? = when {
        password.length < 8 -> "Password must be at least 8 characters."
        password.length > 4096 -> "Password must be at most 4096 characters."
        password.none { it in 'A'..'Z' } -> "Add an uppercase letter (A–Z)."
        password.none { it in 'a'..'z' } -> "Add a lowercase letter (a–z)."
        password.none { it in '0'..'9' } -> "Add a number (0–9)."
        password.none { it in "^$*.[]{}()?\"!@#%&/\\,><':;|_~" } -> "Add a symbol, such as !, @, or #."
        else -> null
    }
}
