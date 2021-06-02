package com.gigforce.core.di.interfaces

interface IBuildConfigVM {
    fun getDrivingCertificateMethod():String
    fun getReferralBaseUrl():String
    fun getCreateUserUrl():String
    fun getCreateOrSendOTPUrl():String
    fun getVerifyOTPURL():String
    fun getGeneratePayslipURL():String
    fun getApplicationID():String
    fun getSendSMSURL():String
}