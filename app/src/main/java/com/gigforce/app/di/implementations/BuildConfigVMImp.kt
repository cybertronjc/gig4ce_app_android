package com.gigforce.app.di.implementations

import com.gigforce.app.BuildConfig
import com.gigforce.core.di.interfaces.IBuildConfigVM
import javax.inject.Inject

class BuildConfigVMImp @Inject constructor() : IBuildConfigVM {

    override fun getDrivingCertificateMethod(): String = BuildConfig.DRIVING_CERTIFICATE_METHOD
    override fun getReferralBaseUrl(): String = BuildConfig.REFERRAL_BASE_URL
    override fun getCreateUserUrl(): String = BuildConfig.CREATE_USER_URL
    override fun getCreateOrSendOTPUrl(): String = BuildConfig.CHECK_USER_OR_SEND_OTP_URL
    override fun getVerifyOTPURL(): String = BuildConfig.VERIFY_OTP_URL
    override fun getGeneratePayslipURL(): String = BuildConfig.GENERATE_PAYSLIP_URL
    override fun getApplicationID(): String = BuildConfig.APPLICATION_ID

}