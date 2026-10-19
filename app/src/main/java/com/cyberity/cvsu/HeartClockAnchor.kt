package com.cyberity.cvsu

/** Device date is deliberately absent. A reboot requires a new server anchor. */
internal data class HeartClockAnchor(val serverAt: Long, val elapsedAt: Long, val boot: Int) {
    fun timeAt(elapsedNow: Long, currentBoot: Int): Long? =
        if (serverAt > 0 && elapsedAt >= 0 && elapsedNow >= elapsedAt && boot >= 0 && boot == currentBoot)
            serverAt + elapsedNow - elapsedAt else null
}
