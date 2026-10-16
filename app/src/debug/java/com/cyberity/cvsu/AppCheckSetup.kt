package com.cyberity.cvsu

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

internal object AppCheckSetup {
    fun install() {
        FirebaseAppCheck.getInstance().apply {
            installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            setTokenAutoRefreshEnabled(true)
            getAppCheckToken(false)
                .addOnSuccessListener { Log.i("CyberityAppCheck", "Debug App Check token verified") }
                .addOnFailureListener { Log.w("CyberityAppCheck", "Debug App Check verification unavailable") }
        }
    }
}
