package com.gigforce.app.modules.client_activation

import com.gigforce.app.BuildConfig
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.ambassador_user_enrollment.models.RegisterMobileNoRequest
import com.gigforce.app.modules.ambassador_user_enrollment.models.RegisterMobileNoResponse
import com.gigforce.app.modules.ambassador_user_enrollment.models.VerifyOtpResponse
import com.gigforce.app.modules.verification.service.CreateUserAccEnrollmentAPi
import com.gigforce.app.utils.network.RetrofitFactory

class ScheduleDrivingTestRepository : BaseFirestoreDBRepository() {
    private val createUserApi: CreateUserAccEnrollmentAPi = com.gigforce.app.modules.verification.service.RetrofitFactory.createUserAccEnrollmentAPi()
    override fun getCollectionName(): String {
        return "JP_Applications"
    }


    suspend fun getDrivingCertificate(_id: String, downloadCertificateID: String) = RetrofitFactory.retrofit(BuildConfig.DRIVING_CERTIFICATE_URL).downloadDrivingLicense(
            _id, downloadCertificateID
    )
}