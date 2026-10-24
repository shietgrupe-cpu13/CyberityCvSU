package com.cyberity.cvsu

import android.app.Application
import com.google.firebase.FirebaseApp

class CyberityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppCheckSetup.install()
        UpdateNotifications.schedule(this)
    }
}
