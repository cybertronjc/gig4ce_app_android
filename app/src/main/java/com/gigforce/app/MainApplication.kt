package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCleverTap()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
        setUpRemoteConfig()
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


    private fun setUpRemoteConfig() {
        FirebaseRemoteConfig.getInstance().apply {

            fetchAndActivate().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Log.d("TAG", "Config params updated")
                } else {
                    Log.d("TAG", "Config params updated")
                }
            }
        }
    }

}