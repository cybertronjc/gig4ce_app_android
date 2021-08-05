package com.gigforce.core.logger

import android.util.Log
import com.gigforce.core.crashlytics.CrashlyticsLogger
import timber.log.Timber

class TimberReleaseTree : Timber.Tree() {

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        when (priority) {
            Log.ERROR -> {
                val finalTag = tag ?: "Gigforce"
                if (t != null)
                    CrashlyticsLogger.e(finalTag, message, t)
                else
                    CrashlyticsLogger.e(finalTag, message)
            }
            else -> {
                val finalTag = tag ?: "Gigforce"
                if (t != null)
                    CrashlyticsLogger.d(finalTag, message, t)
                else
                    CrashlyticsLogger.d(finalTag, message)
            }
        }
    }
}