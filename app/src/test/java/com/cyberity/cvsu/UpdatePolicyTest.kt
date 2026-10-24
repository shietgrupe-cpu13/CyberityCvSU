package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

class UpdatePolicyTest {
    @Test fun onlyExactRepositoryAssetIsAccepted() {
        val url = UpdatePolicy.RELEASE_PREFIX + "v1.0.7/CyberityCvSU.apk"
        assertTrue(UpdatePolicy.assetUrl(url, "v1.0.7"))
        assertFalse(UpdatePolicy.assetUrl(url + "?redirect=elsewhere", "v1.0.7"))
        assertFalse(UpdatePolicy.assetUrl(url.replace("shietgrupe-cpu13", "someone"), "v1.0.7"))
        assertFalse(UpdatePolicy.assetUrl(url.replace("v1.0.7", "v1.0.8"), "v1.0.7"))
        assertFalse(UpdatePolicy.assetUrl(url, "../v1.0.7"))
    }

    @Test fun redirectsCannotDowngradeOrLeakToUntrustedHosts() {
        assertTrue(UpdatePolicy.downloadUrl("https://release-assets.githubusercontent.com/path?signature=123"))
        assertTrue(UpdatePolicy.downloadUrl("https://objects.githubusercontent.com/path"))
        listOf("http://release-assets.githubusercontent.com/path", "https://github.com.attacker.test/path",
            "https://github.com@attacker.test/path", "https://release-assets.githubusercontent.com:8443/path",
            "file:///tmp/update.apk", "https://github.com/other/repo/releases/download/v1.0.7/app.apk",
            "https://release-assets.githubusercontent.com.attacker.test/path").forEach {
            assertFalse(it, UpdatePolicy.downloadUrl(it))
        }
    }

    @Test fun missingMalformedAndChangedHashesAreRejected() {
        val original = MessageDigest.getInstance("SHA-256").digest("valid apk".toByteArray())
        val hex = original.joinToString("") { "%02x".format(it) }
        assertEquals(hex, UpdatePolicy.digest("sha256:$hex"))
        assertNull(UpdatePolicy.digest(""))
        assertNull(UpdatePolicy.digest("sha256:1234"))
        assertNull(UpdatePolicy.digest("sha1:$hex"))
        assertTrue(UpdatePolicy.matchesDigest(original, hex))
        assertFalse(UpdatePolicy.matchesDigest(MessageDigest.getInstance("SHA-256").digest("tampered apk".toByteArray()), hex))
        assertFalse(UpdatePolicy.matchesDigest(original, "not a hash"))
    }

    @Test fun wrongPackageSignerVersionOrDowngradeCannotBeInstalled() {
        fun valid(name: String = "com.cyberity.cvsu", code: Long = 8, version: String = "1.0.7",
            signers: Set<String> = setOf("trusted"), installedSigners: Set<String> = setOf("trusted")) =
            UpdatePolicy.validPackage("com.cyberity.cvsu", name, 7, code, "v1.0.7", version, installedSigners, signers)
        assertTrue(valid())
        assertFalse(valid(name = "com.other.app"))
        assertFalse(valid(code = 7))
        assertFalse(valid(code = 6))
        assertFalse(valid(version = "1.0.8"))
        assertFalse(valid(signers = setOf("attacker")))
        assertFalse(valid(signers = setOf("trusted", "attacker")))
        assertFalse(valid(signers = emptySet(), installedSigners = emptySet()))
    }
}
