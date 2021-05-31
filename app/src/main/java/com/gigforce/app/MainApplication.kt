package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.core.IEventTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.HiltAndroidApp
import io.branch.referral.Branch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

//    var mixpanel: MixpanelAPI? = null
//    private var cleverTapAPI: CleverTapAPI? = null


//    private val firebaseAnalytics: FirebaseAnalytics by lazy {
//        FirebaseAnalytics.getInstance(this)
//    }

//    private val appsFlyerLib: AppsFlyerLib by lazy {
//        AppsFlyerLib.getInstance()
//    }


    override fun onCreate() {
        super.onCreate()
        setUpBranchTool()
        //setupCleverTap()
        //setupMixpanel()
        //setUpAppsFlyer()
        //setUpUserOnAnalyticsAndCrashlytics()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
        setUpRemoteConfig()
    }

    private fun setUpBranchTool() {
        // Branch logging for debugging
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);
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




    companion object {

        const val LOG_TAG = "GigforceApp"
    }
}

//private fun setUpAppsFlyer() {
//
//    AppsFlyerLib.getInstance().apply {
//
//        init(
//            BuildConfig.APPS_FLYER_KEY,
//            appsFlyerConversationListener,
//            this@MainApplication
//        )
//        startTracking(this@MainApplication)
//    }
//}
//
//private fun setUpUserOnAnalyticsAndCrashlytics() {
//    FirebaseAuth.getInstance().currentUser?.let {
//        FirebaseCrashlytics.getInstance().setUserId(it.uid)
//
//        firebaseAnalytics.setUserId(it.uid)
//        cleverTapAPI?.pushProfile(mapOf(
//            "user_id" to it.uid
//        ))
//
//        mixpanel?.identify(it.uid);
//        mixpanel?.getPeople()?.identify(it.uid)
//        mixpanel?.track("User identified")
//
//        appsFlyerLib.setCustomerIdAndTrack(it.uid, applicationContext)
//    }
//}
//
//private fun setupMixpanel() {
//    mixpanel = MixpanelAPI.getInstance(applicationContext, BuildConfig.MIX_PANEL_KEY)
//    mixpanel?.distinctId?.let {
//        Branch.getInstance().setRequestMetadata(
//            "\$mixpanel_distinct_id",
//            it
//        )
//    }
//}
//
//private fun setupCleverTap() {
//    val clevertapDefaultInstance =
//        CleverTapAPI.getDefaultInstance(applicationContext)
//
//    cleverTapAPI = CleverTapAPI.getDefaultInstance(applicationContext)
//    CleverTapAPI.createNotificationChannel(
//        applicationContext,
//        "gigforce-general",
//        "Gigforce",
//        "Gigforce Push Notifications",
//        NotificationManager.IMPORTANCE_MAX,
//        true
//    )
//
//    cleverTapAPI?.pushEvent("MAIN_APP_CREATED")
//}
//
//private val appsFlyerConversationListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
//
//    override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
//        data?.let { cvData ->
//            cvData.map {
//                Log.i(MainApplication.LOG_TAG, "conversion_attribute: ${it.key} = ${it.value}")
//            }
//        }
//    }
//
//    override fun onConversionDataFail(error: String?) {
//        Log.e(MainApplication.LOG_TAG, "error onAttributionFailure : $error")
//    }
//
//    override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
//        data?.map {
//            Log.d(MainApplication.LOG_TAG, "onAppOpen_attribute: ${it.key} = ${it.value}")
//        }
//    }
//
//    override fun onAttributionFailure(error: String?) {
//        Log.e(MainApplication.LOG_TAG, "error onAttributionFailure : $error")
//    }
//}