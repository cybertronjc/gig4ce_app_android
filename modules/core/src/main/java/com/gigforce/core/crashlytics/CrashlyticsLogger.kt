package com.gigforce.core.crashlytics

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

    fun e(
        tag: String,
        occurredWhen: String,
        e: Throwable
    ) {
        firebaseCrashlytics.log("$tag :, Occured when: $occurredWhen")
        firebaseCrashlytics.recordException(e)
    }
}