package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.modules.auth.utils.AppSignatureHelper
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    val MIXPANEL_TOKEN = "536f16151a9da631a385119be6510d56"
    var mixpanel : MixpanelAPI? = null

    override fun onCreate() {
        super.onCreate()
        setupCleverTap()
        setupMixpanel()
        setUpCrashlyticsUser()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
        setUpRemoteConfig()
    }

    private fun setUpCrashlyticsUser() {
        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseCrashlytics.getInstance().setUserId(it.uid)
        }
    }

    private fun setupMixpanel(){
        mixpanel = MixpanelAPI.getInstance(applicationContext, MIXPANEL_TOKEN);
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