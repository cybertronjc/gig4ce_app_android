package com.gigforce.core.base.utilfeatures

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.text.TextUtils
import android.widget.Toast
import java.util.*

class UtilAndValidationImp(var activity:Activity) :UtilAndValidationInterface{

    override fun updateResources(language: String) {
        val locale = Locale(language)
        val config2 = Configuration()
        config2.locale = locale
        // updating locale
        activity?.resources?.updateConfiguration(config2, null)
        Locale.setDefault(locale)
    }



    override fun showToast(Message: String) {
        if (isNullOrWhiteSpace(Message)) return
        val toast = Toast.makeText(activity, Message, Toast.LENGTH_SHORT)
        toast.show()
    }

    // Toast Message
    override fun isNullOrWhiteSpace(str: String): Boolean {
        return if (TextUtils.isEmpty(str)) true else if (str.startsWith("null")) true else false
    }




    override fun showToastLong(Message: String, Duration: Int) {
        if (isNullOrWhiteSpace(Message)) return
        val toast = Toast.makeText(activity, Message, Toast.LENGTH_LONG)
        toast.show()
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