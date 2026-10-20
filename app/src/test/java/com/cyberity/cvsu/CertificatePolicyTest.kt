package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class CertificatePolicyTest {
    private val required = CertificatePolicy.requiredLevels(sampleLearningUnits())
    private val student = UserProfile("202310502", "Student User", yearLevel = 3, fullName = "Student Full Name")

    @Test fun existingProfilesRemainUsableButNeedYearBeforeCertificateDownload() {
        val legacy = student.copy(yearLevel = null)
        assertTrue(legacy.isComplete)
        assertFalse(CertificatePolicy.canDownload(legacy, required, required))
        assertTrue(CertificatePolicy.canDownload(student, required, required))
    }

    @Test fun missingFullNameKeepsLegacyAccountUsableButLocksAllDownloads() {
        val legacy = student.copy(fullName = "")
        assertTrue(legacy.isComplete)
        assertFalse(CertificatePolicy.canDownload(legacy, required, required))
        assertEquals(CertificatePolicy.DownloadKind.LOCKED,
            CertificatePolicy.downloadKind(legacy, required, required, true))
        assertNull(StudentIdRules.validateFullName("  María O’Neil-Santos  "))
        assertNotNull(StudentIdRules.validateFullName(" "))
        assertNotNull(StudentIdRules.validateFullName("a".repeat(101)))
        assertNotNull(StudentIdRules.validateFullName("Student\nName"))
    }

    @Test fun laterDuplicateMustChangeUsernameBeforeCertificateDownload() {
        val duplicate = student.copy(usernameChangeRequired = true)
        assertFalse(duplicate.isComplete)
        assertFalse(CertificatePolicy.canDownload(duplicate, required, required))
    }

    @Test fun middleNameIsOptionalAndCertificateUsesProvidedParts() {
        assertNull(StudentIdRules.validateNameParts("Ana", "", "Cruz"))
        assertEquals("Ana Cruz", StudentIdRules.composeFullName(" Ana ", "  ", " Cruz "))
        assertEquals("Ana Maria Cruz", StudentIdRules.composeFullName("Ana", "Maria", "Cruz"))
        assertNull(StudentIdRules.validateNameParts("A", "", "B"))
        assertNotNull(StudentIdRules.validateNameParts("", "Maria", "Cruz"))
        assertNotNull(StudentIdRules.validateNameParts("Ana", "", " "))
        assertNotNull(StudentIdRules.validateNameParts("Ana", "x".repeat(51), "Cruz"))
        assertEquals("Ana Cruz", student.copy(firstName = "Ana", lastName = "Cruz").certificateName)
        assertEquals(student.fullName, student.certificateName)
    }

    @Test fun optionalWalkthroughAndRewardAreExcludedButEveryRequiredLevelIsNeeded() {
        assertFalse(200 in required)
        assertFalse(450 in required)
        assertTrue(101 in required && 350 in required && 501 in required && 601 in required && 605 in required)
        assertFalse(CertificatePolicy.canDownload(student, required - 605 + 999, required))
        assertFalse(CertificatePolicy.canDownload(student, emptySet(), emptySet()))
    }

    @Test fun missingIdentityAndInvalidYearNeverUnlockCertificate() {
        assertFalse(CertificatePolicy.canDownload(null, required, required))
        assertFalse(CertificatePolicy.canDownload(student.copy(studentId = ""), required, required))
        assertFalse(CertificatePolicy.canDownload(student.copy(yearLevel = 0), required, required))
        assertFalse(CertificatePolicy.canDownload(student.copy(yearLevel = 6), required, required))
    }

    @Test fun certificateNumberIsStableAndAccountSpecific() {
        assertEquals(CertificatePolicy.certificateNumber("student-a"), CertificatePolicy.certificateNumber("student-a"))
        assertNotEquals(CertificatePolicy.certificateNumber("student-a"), CertificatePolicy.certificateNumber("student-b"))
        assertTrue(CertificatePolicy.certificateNumber("student-a").matches(Regex("CYB-[A-F0-9]{12}")))
    }

    @Test fun yearValuesHandleLegacyRecordsWithoutInventingASelection() {
        assertNull(YearLevels.fromStored(null))
        assertNull(YearLevels.fromStored("3"))
        assertNull(YearLevels.fromStored(2.5))
        assertNull(YearLevels.fromStored(6L))
        assertEquals(3, YearLevels.fromStored(3L))
        assertEquals("3rd year", YearLevels.label(3))
        assertEquals("5th year or higher", YearLevels.label(5))
    }

    @Test fun developerCanDownloadOnlyASampleBeforeCompletion() {
        assertEquals(CertificatePolicy.DownloadKind.PREVIEW,
            CertificatePolicy.downloadKind(student, emptySet(), required, true))
        assertEquals(CertificatePolicy.DownloadKind.LOCKED,
            CertificatePolicy.downloadKind(student, emptySet(), required, false))
        assertFalse(CertificatePolicy.canDownload(student, emptySet(), required))
    }

    @Test fun legacyDeveloperCanPreviewMissingYearButNotClaimCompletion() {
        val legacy = student.copy(yearLevel = null)
        assertEquals(CertificatePolicy.DownloadKind.PREVIEW,
            CertificatePolicy.downloadKind(legacy, required, required, true))
        assertEquals(CertificatePolicy.DownloadKind.LOCKED,
            CertificatePolicy.downloadKind(legacy, required, required, false))
        assertEquals(CertificatePolicy.DownloadKind.LOCKED,
            CertificatePolicy.downloadKind(null, emptySet(), required, true))
    }

    @Test fun completedStudentReceivesEarnedCertificateRegardlessOfDeveloperFlag() {
        for (dev in listOf(false, true)) assertEquals(CertificatePolicy.DownloadKind.COMPLETION,
            CertificatePolicy.downloadKind(student, required, required, dev))
    }
}
