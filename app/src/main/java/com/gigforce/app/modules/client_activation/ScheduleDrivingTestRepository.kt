package com.gigforce.app.modules.client_activation

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.utils.network.RetrofitFactory

class ScheduleDrivingTestRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Applications"
    }


    suspend fun getDrivingCertificate(_id: String, downloadCertificateID: String) = RetrofitFactory.retrofit("https://qwny706375.execute-api.ap-south-1.amazonaws.com/default/").downloadDrivingLicense(
            _id, downloadCertificateID
    )

}