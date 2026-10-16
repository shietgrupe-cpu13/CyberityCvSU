package com.cyberity.cvsu

object CertificatePolicy {
    enum class DownloadKind { LOCKED, PREVIEW, COMPLETION }
    const val COURSE_VERSION = "cyberity-awareness-v1"

    /** The skippable walkthrough and reward cache do not count as course requirements. */
    fun requiredLevels(units: List<LearningUnit>): Set<Int> = units.flatMap { it.levels }
        .filter { it.id != 200 && it.type != LevelType.REWARD }.map { it.id }.toSet()

    fun canDownload(profile: UserProfile?, completed: Set<Int>, required: Set<Int>): Boolean =
        profile?.isComplete == true && YearLevels.isValid(profile.yearLevel) &&
            required.isNotEmpty() && completed.containsAll(required)

    fun downloadKind(profile: UserProfile?, completed: Set<Int>, required: Set<Int>, developerMode: Boolean): DownloadKind = when {
        canDownload(profile, completed, required) -> DownloadKind.COMPLETION
        developerMode && profile?.isComplete == true -> DownloadKind.PREVIEW
        else -> DownloadKind.LOCKED
    }

    fun certificateNumber(uid: String): String =
        "CYB-" + LabValidator.sha256("$COURSE_VERSION:$uid").take(12).uppercase(java.util.Locale.ROOT)
}

/** Team-owned template settings; students cannot change the certificate signatories. */
object CertificateBranding {
    // Add the three approved names here later. Signatures remain blank until supplied.
    val signatoryNames = listOf("", "", "")
    const val signatoryTitle = "Project Developer"
}
