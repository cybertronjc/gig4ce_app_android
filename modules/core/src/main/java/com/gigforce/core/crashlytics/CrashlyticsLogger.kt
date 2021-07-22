package com.gigforce.core.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsLogger {

    private val firebaseCrashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    fun d(
        tag: String,
        msg: String
    ) {
        Log.d(tag, msg)
        firebaseCrashlytics.log("$tag : $msg")
    }

    fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable
    ) {
        firebaseCrashlytics.log("$tag :, Occurred when: $occurredWhen")
        firebaseCrashlytics.recordException(e)
        Log.e(tag,"Occurred when: $occurredWhen", e)
    }
}