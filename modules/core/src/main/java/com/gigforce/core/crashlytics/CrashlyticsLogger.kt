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
        firebaseCrashlytics.log("$tag : $msg")
    }

    fun d(
        tag: String,
        msg: String,
        e: Throwable
    ) {
        firebaseCrashlytics.log("$tag : $msg")
        firebaseCrashlytics.recordException(e)
    }

    fun e(
        tag: String,
        message: String
    ) {
        firebaseCrashlytics.log("$tag : $message")
    }

    fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable
    ) {
        firebaseCrashlytics.log("$tag :, Occurred when: $occurredWhen")
        firebaseCrashlytics.recordException(e)
    }
}