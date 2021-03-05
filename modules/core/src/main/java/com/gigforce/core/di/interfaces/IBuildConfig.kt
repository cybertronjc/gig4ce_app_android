package com.gigforce.core.di.interfaces

interface IBuildConfig {
    fun getDrivingCertificateMethod():String
    fun getReferralBaseUrl():String
    fun getCreateUserUrl():String
    fun getCreateOrSendOTPUrl():String
    fun getVerifyOTPURL():String
}