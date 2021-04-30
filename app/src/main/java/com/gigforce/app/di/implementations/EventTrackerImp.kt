package com.gigforce.app.di.implementations

import android.app.Activity
import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.MainApplication
import com.gigforce.app.core.toBundle
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EventTrackerImp @Inject constructor(
        @ApplicationContext val context: Context
) : IEventTracker {

    var mixpanel: MixpanelAPI? = (context as MainApplication).mixpanel

    private var cleverTapApi: CleverTapAPI? = CleverTapAPI.getDefaultInstance(context)

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
    }

    override fun setUserProperty(props: Map<String, Any>) {
        mixpanel?.registerSuperPropertiesMap(props)
    }

    override fun removeUserProperty(prop: String) {
        mixpanel?.unregisterSuperProperty(prop)
    }

    override fun pushEvent(args: TrackingEventArgs) {
        Log.d("EventTrackerImp", "---Event Pushed-------")
        Log.d("EventTrackerImp", "Event Name : ${args.eventName}")
        Log.d("EventTrackerImp", "Event Properties : ${args.props}")

        logEventOnMixPanel(args)
        logEventOnFirebaseAnalytics(args)
        logEventOnCleverTap(args)
        logEventOnAppsFlyer(args)
    }

    override fun setProfileProperty(args: ProfilePropArgs) {
        logProfilePropertiesOnMixpanel(args)
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

    private fun logProfilePropertiesOnMixpanel(args: ProfilePropArgs){
        try {
            mixpanel?.people?.set(args.propertyName, args.propertyValue)
        }
        catch (e: Exception){
            e.printStackTrace()
            CrashlyticsLogger.e("EventTrackerImp", "While logging profile property on MixPanel", e)
        }
    }
}