package com.cyberity.cvsu

import java.net.URI
import java.security.MessageDigest

internal object UpdatePolicy {
    const val MAX_APK_BYTES = 150L * 1024 * 1024
    const val RELEASE_PREFIX = "https://github.com/shietgrupe-cpu13/CyberityCvSU/releases/download/"

    fun assetUrl(url: String, version: String): Boolean =
        AppVersion.parts(version) != null && url == RELEASE_PREFIX + version + "/CyberityCvSU.apk"

    fun downloadUrl(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.userInfo == null && uri.port == -1 && uri.fragment == null &&
            (uri.host == "github.com" && uri.path.startsWith("/shietgrupe-cpu13/CyberityCvSU/releases/download/") ||
                uri.host in setOf("release-assets.githubusercontent.com", "objects.githubusercontent.com"))
    }.getOrDefault(false)

    fun digest(value: String): String? = value.takeIf { it.matches(Regex("sha256:[a-fA-F0-9]{64}")) }
        ?.substringAfter(':')?.lowercase()

    fun matchesDigest(actual: ByteArray, expected: String): Boolean =
        expected.matches(Regex("[a-f0-9]{64}")) && MessageDigest.isEqual(actual,
            expected.chunked(2).map { it.toInt(16).toByte() }.toByteArray())

    fun validPackage(installedName: String, candidateName: String?, installedCode: Long,
        candidateCode: Long, expectedVersion: String, candidateVersion: String?,
        installedSigners: Set<String>, candidateSigners: Set<String>): Boolean =
        installedName == candidateName && candidateCode > installedCode &&
            candidateVersion == expectedVersion.removePrefix("v") &&
            installedSigners.isNotEmpty() && installedSigners == candidateSigners
}
