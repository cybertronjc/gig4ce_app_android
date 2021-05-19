package com.gigforce.core.base.utilfeatures

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ActivityContext
import java.util.*
import javax.inject.Inject

class CommonUtilImp @Inject constructor(
    @ActivityContext var activity:Activity) :CommonUtilInterface{

    override fun updateResources(language: String) {
        val locale = Locale(language)
        val config2 = Configuration()
        config2.locale = locale
        // updating locale
        activity?.resources?.updateConfiguration(config2, null)
        Locale.setDefault(locale)
    }

    override fun getCurrentVersion(): String {
        try {
            val pInfo: PackageInfo =
                activity?.applicationContext!!.packageManager.getPackageInfo(
                    activity.getPackageName(),
                    0
                )
            val version = pInfo.versionName
            val versionCode = pInfo.versionCode
            return version+"("+versionCode+")"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}