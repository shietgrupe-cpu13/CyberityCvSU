package com.cyberity.cvsu

import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Read-only diagnostic: report the current account UID, never tokens or credentials. */
class DeveloperAccountProbeTest {
    @Test fun reportCurrentAccountUid() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        assertNotNull("Sign in to Cyberity on the phone first", uid)
        InstrumentationRegistry.getInstrumentation().sendStatus(0, Bundle().apply {
            putString("developerTargetUid", uid)
        })
    }
}
