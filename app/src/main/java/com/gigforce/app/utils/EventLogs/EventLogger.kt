package com.gigforce.app.utils.EventLogs

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*

class EventLogger (context: Context) {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    init {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun Log(eventName: String, bundle: Bundle){
        mFirebaseAnalytics.logEvent(eventName, bundle)
    }

    fun setUserId(uid:String) {
        mFirebaseAnalytics.setUserId(uid)
    }

    fun setUserProperty(name: String, value: String){
        mFirebaseAnalytics.setUserProperty(name, value)
        // todo: set into CleverTap As Well
    }
}