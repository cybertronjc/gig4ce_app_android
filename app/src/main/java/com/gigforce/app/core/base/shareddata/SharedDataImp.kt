package com.gigforce.app.core.base.shareddata

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gigforce.app.core.CoreConstants
import com.gigforce.app.utils.AppConstants

class SharedDataImp : SharedDataInterface {
    lateinit var SP: SharedPreferences
    var editor: SharedPreferences.Editor? = null

    constructor(activity: Activity) {
        SP = activity?.getSharedPreferences(
            CoreConstants.SHARED_PREFERENCE_DB,
            Context.MODE_PRIVATE
        )!!
        this.editor = SP.edit()
    }

    //common methods for sharedPreference
    private fun saveSharedData(Key: String?, Value: String?): Boolean {
        return try {
            editor?.putString(Key, Value)
            editor?.commit()
            true
        } catch (ex: Exception) {
            Log.e("Error:", ex.toString())
            false
        }
    }

    // for delete
    private fun removeSavedShareData(key: String?): Boolean {
        return try {
            editor?.remove(key)
            editor?.commit()
            true
        } catch (ex: Exception) {
            Log.e("Error:", ex.toString())
            false
        }
    }

    private fun getSharedData(key: String?, defValue: String?): String? {
        return SP.getString(key, defValue)
    }
    //common methods for sharedPreference end


    override fun getLastStoredDeviceLanguage(): String? {
        return getSharedData(AppConstants.DEVICE_LANGUAGE, "")
    }

    override fun saveDeviceLanguage(deviceLanguage: String) {
        saveSharedData(AppConstants.DEVICE_LANGUAGE, deviceLanguage)
    }

    override fun saveAppLanuageCode(appLanguage: String) {
        saveSharedData(AppConstants.APP_LANGUAGE_CODE, appLanguage)
    }

    override fun getAppLanguageCode(): String? {
        return getSharedData(AppConstants.APP_LANGUAGE_CODE, "")
    }

    override fun saveAppLanguageName(appLanguage: String) {
        saveSharedData(AppConstants.APP_LANGUAGE_NAME, appLanguage)
    }

    override fun getAppLanguageName(): String? {
        return getSharedData(AppConstants.APP_LANGUAGE_NAME, "")
    }

    override fun saveIntroCompleted() {
        saveSharedData(AppConstants.INTRO_COMPLETE, "true")
    }

    override fun getIntroCompleted(): String? {
        return getSharedData(AppConstants.INTRO_COMPLETE, "")
    }

    override fun removeIntroComplete() {
        removeSavedShareData(AppConstants.INTRO_COMPLETE)
    }

    override fun saveOnBoardingCompleted() {
        saveSharedData(AppConstants.ON_BOARDING_COMPLETED, "true")
    }

    override fun isOnBoardingCompleted(): Boolean? {
        return getSharedData(AppConstants.ON_BOARDING_COMPLETED, "").equals("true")
    }

    override fun saveAllMobileNumber(allMobileNumber: String) {
        saveSharedData(AppConstants.ALL_MOBILE_NUMBERS_USED, allMobileNumber)
    }

    override fun getAllMobileNumber(): String? {
        return getSharedData(AppConstants.ALL_MOBILE_NUMBERS_USED, "")
    }

    override fun saveData(key: String, value: String?) {
        saveSharedData(key, value)
    }

    override fun getData(key: String?): String? {
        return getSharedData(key, "")
    }

    override fun remove(key: String?) {
        removeSavedShareData(key)
    }
}