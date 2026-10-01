package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class LabValidatorTest {
    private val flag = "CYBERITY{first_flag_captured}"
    private val answer = LabAnswer.Flag(sha256 = LabValidator.sha256(flag.lowercase()))

    private fun accepts(submitted: String) = LabValidator.isCorrect(answer, submitted, null)

    @Test
    fun acceptsTheFullFlag() {
        assertTrue(accepts(flag))
        assertTrue(accepts("  cyberity{first_flag_captured}  "))
    }

    @Test
    fun acceptsTheInnerTokenAlone() {
        assertTrue(accepts("first_flag_captured"))
    }

    @Test
    fun forgivesAMissingClosingBrace() {
        // What a long-press copy in the simulation typically yields.
        assertTrue(accepts("CYBERITY{first_flag_captured"))
    }

    @Test
    fun stillRejectsWrongFlags() {
        assertFalse(accepts("CYBERITY{first_flag_captured_x}"))
        assertFalse(accepts("CYBERITY{first_flag_captur"))
        assertFalse(accepts("CYBERITY{"))
        assertFalse(accepts(""))
    }
}
