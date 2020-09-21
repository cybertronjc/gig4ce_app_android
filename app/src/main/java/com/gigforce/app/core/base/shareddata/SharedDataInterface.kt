package com.gigforce.app.core.base.shareddata

interface SharedDataInterface {
    fun getLastStoredDeviceLanguage(): String?
    fun saveDeviceLanguage(deviceLanguage: String)

    fun saveAppLanuageCode(appLanguage: String)
    fun getAppLanguageCode(): String?
    fun saveAppLanguageName(appLanguage: String)
    fun getAppLanguageName(): String?

    fun saveIntroCompleted()
    fun getIntroCompleted(): String?
    fun removeIntroComplete()
    fun saveOnBoardingCompleted()
    fun isOnBoardingCompleted(): Boolean?

    fun saveAllMobileNumber(allMobileNumber: String)
    fun getAllMobileNumber(): String?
    fun saveData(key: String, value: String?)
    fun getData(key: String?): String?
    fun remove(key: String?)
}