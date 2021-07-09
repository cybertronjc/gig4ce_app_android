package com.gigforce.app.di.implementations

import com.gigforce.app.BuildConfig
import com.gigforce.core.di.interfaces.IBuildConfig
import javax.inject.Inject

class BuildConfigImp @Inject constructor(): IBuildConfig {

    override fun getDrivingCertificateMethod(): String = BuildConfig.DRIVING_CERTIFICATE_METHOD
    override fun getReferralBaseUrl(): String = BuildConfig.REFERRAL_BASE_URL
    override fun getCreateUserUrl(): String = BuildConfig.CREATE_USER_URL
    override fun getCreateOrSendOTPUrl(): String = BuildConfig.CHECK_USER_OR_SEND_OTP_URL
    override fun getVerifyOTPURL(): String = BuildConfig.VERIFY_OTP_URL
    override fun getGeneratePayslipURL(): String = BuildConfig.GENERATE_PAYSLIP_URL
    override fun getApplicationID(): String = BuildConfig.APPLICATION_ID
    override fun getSendSMSURL() : String = BuildConfig.SEND_SMS_URL
    override fun getFeaturesIconLocationUrl(): String = BuildConfig.ALL_FEATURES_ICONS_STORAGE_URL
}