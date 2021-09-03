package com.gigforce.common_ui

import android.content.Context
import android.webkit.WebView
import androidx.annotation.MainThread
import com.gigforce.core.base.BaseActivity

class WebViewLocaleHelper(
    private val activity: BaseActivity
    ) {

    private var requireWorkaround = true

    @MainThread
    fun implementWorkaround() {
        if (requireWorkaround) {
            requireWorkaround = false
            try {
                WebView(activity).destroy()

            } catch (e: Exception) {

            } finally {
                val locale = activity.currentLanguage
                activity.setLanguage(locale)
            }
        }
    }
}