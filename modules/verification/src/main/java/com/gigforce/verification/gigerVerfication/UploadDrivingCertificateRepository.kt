package com.gigforce.verification.gigerVerfication

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class UploadDrivingCertificateRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Applications"
    }



}