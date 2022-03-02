package com.gigforce.app.di.implementations

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.gigforce.app.BuildConfig
import com.gigforce.app.MainApplication
import com.gigforce.app.background.workers.AttendanceWorker
import com.gigforce.app.eventbridge.EventBridgeRepo
import com.gigforce.core.extensions.toBundle
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.Properties
import dagger.hilt.android.qualifiers.ApplicationContext
import io.branch.referral.Branch
import kotlinx.coroutines.*
import javax.inject.Inject

class EventTrackerImp @Inject constructor(
        @ApplicationContext val context: Context
) : IEventTracker {

    //var mixpanel: MixpanelAPI? = (context as MainApplication).mixpanel

    var mixpanel: MixpanelAPI? = MixpanelAPI.getInstance(context, BuildConfig.MIX_PANEL_KEY)

    var eventBridgeRepo = EventBridgeRepo(BuildConfig.EVENT_BRIDGE_URL)

    private var moEngageHelper: MoEHelper? = MoEHelper.getInstance(context)

    private val appsFlyerLib: AppsFlyerLib by lazy {
        AppsFlyerLib.getInstance()
    }

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    override fun setUserId(userId: String) {
        mixpanel?.identify(userId);
        mixpanel?.getPeople()?.identify(userId)
        mixpanel?.track("User identified")

        firebaseAnalytics.setUserId(userId)
        appsFlyerLib.setCustomerIdAndTrack(userId, context.applicationContext)

        //branch to mixpanel
        mixpanel?.distinctId?.let {
            Branch.getInstance().setRequestMetadata(
                "\$mixpanel_distinct_id",
                it
            )
        }
        moEngageHelper?.setUniqueId(userId)
        moEngageHelper?.setNumber(userId)
    }

    override fun setUserProperty(props: Map<String, Any>) {
        mixpanel?.registerSuperPropertiesMap(props)
    }

    override fun removeUserProperty(prop: String) {
        mixpanel?.unregisterSuperProperty(prop)
    }

    override fun setUserName(name: String){
        moEngageHelper?.setFirstName(name)
    }


    override fun pushEvent(args: TrackingEventArgs) {
        Log.d("EventTrackerImp", "---Event Pushed-------")
        Log.d("EventTrackerImp", "Event Name : ${args.eventName}")
        Log.d("EventTrackerImp", "Event Properties : ${args.props}")

        logEventOnMixPanel(args)
        logEventOnFirebaseAnalytics(args)
        logEventOnAppsFlyer(args)
        logEventOnMoEngage(args)
        for(i in 1..50) {
            if(args.eventName == "attendance")
            logEventOnEventBridge(args)
        }
    }

    private fun logEventOnEventBridge(args: TrackingEventArgs) {
        Log.e("attendanceevent", args.eventName)
        val attendanceBuilder = OneTimeWorkRequestBuilder<AttendanceWorker>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString("event_key",args.eventName)
        args.props?.forEach {
            when(it.value){
                is String -> {dataBuilder.putString(it.key, it.value as String)}
                is Int -> {dataBuilder.putInt(it.key, it.value as Int)}
                is Boolean -> {dataBuilder.putBoolean(it.key,it.value as Boolean)}
                is Long -> {dataBuilder.putLong(it.key,it.value as Long)}
            }
        }
        attendanceBuilder.setInputData(dataBuilder.build())
        val workManager = WorkManager.getInstance(context)
        val continuation = workManager.beginWith(attendanceBuilder.build())
        continuation.enqueue()
    }


    override fun setUpAnalyticsTools(){
        setupBranchWithMixpanel()
        setUpAppsFlyer()
    }

    private fun setupBranchWithMixpanel() {
        mixpanel?.distinctId?.let {
            Branch.getInstance().setRequestMetadata(
                "\$mixpanel_distinct_id",
                it
            )
        }
    }

    private fun setUpBranchTool() {
        // Branch logging for debugging
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(context);


    }

    private fun setUpAppsFlyer() {

        appsFlyerLib.apply {

            init(
                BuildConfig.APPS_FLYER_KEY,
                appsFlyerConversationListener,
                context
            )
            startTracking(context)
        }
    }
    private val appsFlyerConversationListener: AppsFlyerConversionListener = object :
        AppsFlyerConversionListener {

        override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
            data?.let { cvData ->
                cvData.map {
                    Log.i(MainApplication.LOG_TAG, "conversion_attribute: ${it.key} = ${it.value}")
                }
            }
        }

        override fun onConversionDataFail(error: String?) {
            Log.e(MainApplication.LOG_TAG, "error onAttributionFailure : $error")
        }

        override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
            data?.map {
                Log.d(MainApplication.LOG_TAG, "onAppOpen_attribute: ${it.key} = ${it.value}")
            }
        }

        override fun onAttributionFailure(error: String?) {
            Log.e(MainApplication.LOG_TAG, "error onAttributionFailure : $error")
        }
    }

    override fun setProfileProperty(args: ProfilePropArgs) {
        logProfilePropertiesOnMixpanel(args)
        logProfilePropertiesOnMoEngage(args)
    }

    override fun logoutUserFromAnalytics(){
        mixpanel?.reset()
        moEngageHelper?.logoutUser()
    }


    private fun logEventOnAppsFlyer(args: TrackingEventArgs) {
        try {
            appsFlyerLib.trackEvent(context,
                    args.eventName,
                    args.props
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging event on AppsFlyer", e)
        }
    }


    private fun logEventOnFirebaseAnalytics(args: TrackingEventArgs) {
        try {
            firebaseAnalytics.logEvent(args.eventName, args.props?.toBundle())
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging event on Firebase Analytics", e)
        }
    }

    private fun logEventOnMixPanel(args: TrackingEventArgs) {
        try {
            mixpanel?.trackMap(args.eventName, args.props)
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging event on MixPanel", e)
        }
    }

    private fun logEventOnMoEngage(args: TrackingEventArgs) {
        try {
            val properties = Properties()
            args.props?.forEach {
                properties.addAttribute(it.key, it.value)
            }
            Log.d("properties", properties.getPayload().toString())
            moEngageHelper?.trackEvent(args.eventName, properties)
        }
        catch (e: Exception){
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging event on MoEngage", e)
        }
    }

    private fun logProfilePropertiesOnMixpanel(args: ProfilePropArgs){
        try {
            mixpanel?.people?.set(args.propertyName, args.propertyValue)
        }
        catch (e: Exception){
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging profile property on MixPanel", e)
        }
    }

    private fun logProfilePropertiesOnMoEngage(args: ProfilePropArgs) {
        try {
            moEngageHelper?.setUserAttribute(args.propertyName, args.propertyValue.toString())
        }
        catch (e: Exception){
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging profile property on MoEngage", e)
        }
    }
}