package com.gigforce.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.gigforce.app.di.implementations.SharedPreAndCommonUtilDataImp
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.core.model.AppStatus
import dagger.hilt.android.HiltAndroidApp
import io.branch.referral.Branch

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
    lateinit var sp: SharedPreAndCommonUtilInterface
    var moEngage = MoEngage.Builder(this, BuildConfig.MOENGAGE_KEY)
        .configureNotificationMetaData(NotificationConfig(R.drawable.ic_notification_icon, R.drawable.ic_notification_icon, R.color.colorPrimary, null, true, isBuildingBackStackEnabled = false, isLargeIconDisplayEnabled = true))
        .configureFcm(FcmConfig(false))
        .build()

    override fun onCreate() {
        super.onCreate()
        setUpBranchTool()
        setUpMoengage()
        //setupCleverTap()
        //setupMixpanel()
        //setUpAppsFlyer()
        //setUpUserOnAnalyticsAndCrashlytics()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
        setUpRemoteConfig()
        initFirebaseAuthListener()
    }

    private fun initFirebaseAuthListener() {
        FirebaseAuthStateListener.getInstance()
    }

    private fun setUpBranchTool() {
        // Branch logging for debugging
        Branch.enableLogging()

        // Branch object initialization
        Branch.getAutoInstance(this)
        Branch.getAutoInstance(this).enableFacebookAppLinkCheck()

    }

    private fun setUpMoengage() {
        MoEngage.initialise(moEngage)
        // install update differentiation
        trackInstallOrUpdate()
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

    /**
     * Tell MoEngage SDK whether the user is a new user of the application or an existing user.
     */
    private fun trackInstallOrUpdate() {
        //keys are just sample keys, use suitable keys for the apps
        sp = SharedPreAndCommonUtilDataImp(this)
        var appStatus = AppStatus.INSTALL
        if (sp.getDataBoolean("has_sent_install") == true) {
            if (sp.getDataBoolean("existing") == true) {
                appStatus = AppStatus.UPDATE
            }
            // passing install/update to MoEngage SDK
            MoEHelper.getInstance(this).setAppStatus(appStatus)
            sp.saveDataBoolean("has_sent_install", true)
            sp.saveDataBoolean("existing", true)
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