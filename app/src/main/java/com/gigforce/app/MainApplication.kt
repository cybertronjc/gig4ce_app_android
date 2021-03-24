package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.clevertap.android.sdk.CleverTapAPI
import com.facebook.FacebookSdk;
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCleverTap()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
    }

    private fun setupCleverTap() {
        val clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)

        val cleverTapAPI = CleverTapAPI.getDefaultInstance(applicationContext)
        CleverTapAPI.createNotificationChannel(
            applicationContext,
            "gigforce-general",
            "Gigforce",
            "Gigforce Push Notifications",
            NotificationManager.IMPORTANCE_MAX,
            true
        )

        cleverTapAPI?.pushEvent("MAIN_APP_CREATED")
    }

}