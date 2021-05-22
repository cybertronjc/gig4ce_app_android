package com.gigforce.app.core.base.shareddata

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gigforce.app.core.CoreConstants
import com.gigforce.app.utils.AppConstants
import com.gigforce.core.utils.SharedDataInterface
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class SharedDataImp  : SharedDataInterface {
    var SP: SharedPreferences
    var editor: SharedPreferences.Editor? = null

    @Inject constructor(@ActivityContext context: Context) {
        SP = context?.getSharedPreferences(
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

    private fun saveSharedDataBoolean(Key: String?, Value: Boolean): Boolean {
        return try {
            editor?.putBoolean(Key, Value)
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

    private fun getSharedDataBoolean(key: String?, defValue: Boolean): Boolean? {
        return SP.getBoolean(key, defValue)
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

    override fun saveDataBoolean(key: String, value: Boolean?) {
        saveSharedDataBoolean(key, value!!)
    }


    override fun getData(key: String?): String? {
        return getSharedData(key, "")
    }

    override fun getDataBoolean(key: String?): Boolean? {
        return getSharedDataBoolean(key, false)
    }

    override fun remove(key: String?) {
        removeSavedShareData(key)
    }

    override fun saveInt(key: String?, value: Int) {

        editor?.putInt(key, value)
        editor?.commit()

    }

    override fun getInt(key: String?): Int {
        return SP.getInt(key, 0)
    }

}