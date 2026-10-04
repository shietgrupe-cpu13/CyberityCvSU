package com.cyberity.cvsu

/** Local request throttling only. Firebase remains the authentication authority. */
internal class AuthThrottle(
    initial: State = State(),
    private val clock: () -> Long,
    private val save: (State) -> Unit = {},
    private val requestIntervalMillis: Long = 1_000L
) {
    data class State(
        val failures: Int = 0,
        val penalty: Int = 0,
        val blockedUntil: Long = 0,
        val lastFailure: Long = 0,
        val nextRequest: Long = 0
    )

    private var state = initial
    private var inFlight = false

    @Synchronized fun retryAfterSeconds(): Long =
        ((maxOf(state.blockedUntil, state.nextRequest) - clock()).coerceAtLeast(0) + 999) / 1_000

    /** Reserve before making a request, including before its Compose UI redraws. */
    @Synchronized fun tryStart(): Boolean {
        if (inFlight || retryAfterSeconds() > 0) return false
        val now = clock()
        if (now - state.lastFailure >= IDLE_RESET_MILLIS) {
            state = State(nextRequest = state.nextRequest)
        }
        inFlight = true
        update(state.copy(nextRequest = now + requestIntervalMillis))
        return true
    }

    @Synchronized fun rejected() {
        val failures = state.failures + 1
        if (failures >= MAX_FAILURES) {
            val cooldown = (30_000L shl state.penalty.coerceIn(0, 5)).coerceAtMost(MAX_COOLDOWN_MILLIS)
            update(state.copy(failures = 0, penalty = (state.penalty + 1).coerceAtMost(5),
                blockedUntil = clock() + cooldown, lastFailure = clock()))
        } else {
            update(state.copy(failures = failures, lastFailure = clock()))
        }
        inFlight = false
    }

    @Synchronized fun serverThrottled() {
        update(state.copy(blockedUntil = maxOf(state.blockedUntil, clock() + 60_000L), lastFailure = clock()))
        inFlight = false
    }

    /** A network error or an MFA challenge is not an incorrect credential. */
    @Synchronized fun finished() { inFlight = false }

    /** Reset only once the complete sign-in (including MFA) succeeds. */
    @Synchronized fun authenticated() {
        update(State())
        inFlight = false
    }

    private fun update(value: State) {
        state = value
        save(value)
    }

    companion object {
        const val MAX_FAILURES = 5
        const val IDLE_RESET_MILLIS = 15 * 60_000L
        const val MAX_COOLDOWN_MILLIS = 15 * 60_000L
    }
}
