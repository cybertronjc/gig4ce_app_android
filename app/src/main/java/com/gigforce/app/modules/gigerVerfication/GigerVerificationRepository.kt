package com.gigforce.app.modules.gigerVerfication

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigerVerificationRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Verification"
    }
}