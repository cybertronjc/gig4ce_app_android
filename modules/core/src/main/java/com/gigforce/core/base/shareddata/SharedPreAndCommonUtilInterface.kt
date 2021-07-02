package com.gigforce.core.base.shareddata

interface SharedPreAndCommonUtilInterface {
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
    fun saveDataBoolean(key: String, value: Boolean?)
    fun getData(key: String?): String?
    fun getDataBoolean(key: String?): Boolean?
    fun remove(key: String?)
    fun saveInt(key: String?, value: Int)
    fun getInt(key: String?): Int
    fun updateResources(language: String)
    fun getCurrentVersion(): String
    fun saveLong(key: String?, value: Long)
    fun getLong(key: String?): Long

    fun saveLoggedInUserName(username : String)
    fun getLoggedInUserName():String
    fun saveLoggedInMobileNumber(mobileno : String)
    fun getLoggedInMobileNumber():String
    fun saveUserProfilePic(profilePic : String)
    fun getUserProfilePic() : String

}