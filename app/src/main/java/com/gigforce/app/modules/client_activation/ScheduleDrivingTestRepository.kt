package com.gigforce.app.modules.client_activation

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class ScheduleDrivingTestRepository : BaseFirestoreDBRepository() {
    //    private val createUserApi: CreateUserAccEnrollmentAPi = com.gigforce.app.modules.verification.service.RetrofitFactory.createUserAccEnrollmentAPi()
    override fun getCollectionName(): String {
        return "JP_Applications"
    }


//    suspend fun getDrivingCertificate(_id: String, downloadCertificateID: String) = RetrofitFactory.retrofit(BuildConfig.DRIVING_CERTIFICATE_URL).downloadDrivingLicense(
//            _id, downloadCertificateID
//    )
}