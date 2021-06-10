package com.gigforce.app.di.implementations

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.BuildConfig
import com.gigforce.app.MainApplication
import com.gigforce.core.extensions.toBundle
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.MoEngage
import com.moengage.core.Properties
import dagger.hilt.android.qualifiers.ApplicationContext
import io.branch.referral.Branch
import javax.inject.Inject

class EventTrackerImp @Inject constructor(
        @ApplicationContext val context: Context
) : IEventTracker {

    //var mixpanel: MixpanelAPI? = (context as MainApplication).mixpanel

    var mixpanel: MixpanelAPI? = MixpanelAPI.getInstance(context, BuildConfig.MIX_PANEL_KEY)


    private var cleverTapApi: CleverTapAPI? = CleverTapAPI.getDefaultInstance(context)
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
        cleverTapApi?.pushProfile(mapOf(
                "user_id" to userId
        ))
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
        logEventOnCleverTap(args)
        logEventOnAppsFlyer(args)
        logEventOnMoEngage(args)
    }



    override fun setUpAnalyticsTools(){
        setupCleverTap()
        setupBranchWithMixpanel()
        setUpAppsFlyer()
    }


    //setup clevertap
    private fun setupCleverTap() {
        CleverTapAPI.createNotificationChannel(
            context,
            "gigforce-general",
            "Gigforce",
            "Gigforce Push Notifications",
            NotificationManager.IMPORTANCE_MAX,
            true
        )

        cleverTapApi?.pushEvent("MAIN_APP_CREATED")
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

    private fun logEventOnCleverTap(args: TrackingEventArgs) {
        try {
            cleverTapApi?.pushEvent(args.eventName, args.props)
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging event on CleverTap", e)
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