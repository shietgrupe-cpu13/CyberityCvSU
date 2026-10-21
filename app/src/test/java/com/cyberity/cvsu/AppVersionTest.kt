package com.cyberity.cvsu
import org.junit.Assert.*
import org.junit.Test
class AppVersionTest {
    @Test fun comparesVersionNumbersInsteadOfText() {
        assertTrue(AppVersion.isNewer("v1.0.10", "1.0.9"))
        assertTrue(AppVersion.isNewer("v2.0", "1.9.99"))
        assertFalse(AppVersion.isNewer("v1.0.3", "1.0.3"))
        assertFalse(AppVersion.isNewer("v1.0", "1.0.0"))
        assertFalse(AppVersion.isNewer("v1.0.2", "1.0.3"))
        assertNull(AppVersion.parts("v1.0.4-beta"))
        assertNull(AppVersion.parts("not a version"))
    }
}
