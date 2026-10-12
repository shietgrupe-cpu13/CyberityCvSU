package com.cyberity.cvsu

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

internal object AppCheckSetup {
    fun install() {
        FirebaseAppCheck.getInstance().apply {
            installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            setTokenAutoRefreshEnabled(true)
        }
    }
}
