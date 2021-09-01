package com.gigforce.core.base

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import java.util.*

open class BaseActivity : AppCompatActivity(), OnLocaleChangedListener {

    private val localizationDelegate = LocalizationActivityDelegate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        localizationDelegate.addOnLocaleChangedListener(this)
        localizationDelegate.onCreate()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        localizationDelegate.onResume(this)
    }

    override fun attachBaseContext(newBase: Context) {
        applyOverrideConfiguration(localizationDelegate.updateConfigurationLocale(newBase))
        super.attachBaseContext(newBase)
    }

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }

    override fun getResources(): Resources {
        return localizationDelegate.getResources(super.getResources())
    }

    fun setLanguage(language: String?) {
        localizationDelegate.setLanguage(this, language!!)
    }

    fun setLanguage(locale: Locale?) {
        localizationDelegate.setLanguage(this, locale!!)
    }

    val currentLanguage: Locale
        get() = localizationDelegate.getLanguage(this)

    // Just override method locale change event
    override fun onBeforeLocaleChanged() {}
    override fun onAfterLocaleChanged() {}

     fun fixupLocale(ctx: Context, newLocale: Locale?) {
        val res = ctx.resources
        val config: Configuration = res.configuration
        val curLocale = getLocale(config)
        if (!curLocale.equals(newLocale)) {
            Locale.setDefault(newLocale)
            val conf = Configuration(config)
            conf.setLocale(newLocale)
            res.updateConfiguration(conf, res.displayMetrics)
        }
    }

      fun getLocale(config: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.getLocales().get(0)
        } else {
            config.locale
        }
    }
}