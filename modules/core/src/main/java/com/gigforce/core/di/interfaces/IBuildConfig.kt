package com.gigforce.core.di.interfaces

interface IBuildConfig {
    fun getDrivingCertificateMethod():String
    fun getReferralBaseUrl():String
    fun getCreateUserUrl():String
    fun getCreateOrSendOTPUrl():String
    fun getVerifyOTPURL():String
    fun getGeneratePayslipURL():String
    fun getApplicationID():String
    fun getSendSMSURL():String
    fun getFeaturesIconLocationUrl() : String
    val debugBuild : Boolean
    val baseUrl : String
}