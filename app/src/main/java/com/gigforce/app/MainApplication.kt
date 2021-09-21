package com.gigforce.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ProcessLifecycleOwner
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import com.gigforce.app.di.implementations.SharedPreAndCommonUtilDataImp
import com.gigforce.app.notification.NotificationConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.logger.TimberReleaseTree
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.core.model.AppStatus
import dagger.hilt.android.HiltAndroidApp
import io.branch.referral.Branch
import timber.log.Timber
import java.util.*

@HiltAndroidApp
class MainApplication : Application() {

    private val localizationDelegate = LocalizationApplicationDelegate()

    lateinit var sp: SharedPreAndCommonUtilInterface
    var moEngage = MoEngage.Builder(this, BuildConfig.MOENGAGE_KEY)
        .configureNotificationMetaData(
            NotificationConfig(
                R.drawable.ic_notification_icon,
                R.drawable.ic_notification_icon,
                R.color.colorPrimary,
                null,
                true,
                isBuildingBackStackEnabled = false,
                isLargeIconDisplayEnabled = true
            )
        )
        .configureFcm(FcmConfig(false))
        .build()

    override fun onCreate() {
        super.onCreate()
        WebView(this).destroy()
        setUpBranchTool()
        setUpMoengage()
        //setupMixpanel()
        //setUpAppsFlyer()
        //setUpUserOnAnalyticsAndCrashlytics()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
        setUpRemoteConfig()
        initFirebaseAuthListener()
        subscribeToFirebaseMessageingTopics()
        initLogger()
    }
    

    private fun initLogger() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(TimberReleaseTree())
    }

    private fun subscribeToFirebaseMessageingTopics() {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(NotificationConstants.TOPICS.TOPIC_SYNC_DATA)
            .addOnSuccessListener {
                Log.d(LOG_TAG, "subscibed to topic sync data")
            }.addOnFailureListener {
                Log.e(LOG_TAG, "Unable to subscribe to sync data topic",it)
            }
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


    override fun attachBaseContext(base: Context) {
        localizationDelegate.setDefaultLanguage(base, Locale.ENGLISH)
        super.attachBaseContext(localizationDelegate.attachBaseContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localizationDelegate.onConfigurationChanged(this)
    }

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }

    override fun getResources(): Resources {
        return localizationDelegate.getResources(baseContext, super.getResources())
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