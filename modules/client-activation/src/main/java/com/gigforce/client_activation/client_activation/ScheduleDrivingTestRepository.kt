package com.gigforce.client_activation.client_activation

import com.gigforce.core.fb.BaseFirestoreDBRepository

class ScheduleDrivingTestRepository : BaseFirestoreDBRepository() {
    //    private val createUserApi: CreateUserAccEnrollmentAPi = com.gigforce.verification.oldverification.service.RetrofitFactory.createUserAccEnrollmentAPi()
    override fun getCollectionName(): String {
        return "JP_Applications"
    }


//    suspend fun getDrivingCertificate(_id: String, downloadCertificateID: String) = RetrofitFactory.retrofit(BuildConfig.DRIVING_CERTIFICATE_URL).downloadDrivingLicense(
//            _id, downloadCertificateID
//    )
}