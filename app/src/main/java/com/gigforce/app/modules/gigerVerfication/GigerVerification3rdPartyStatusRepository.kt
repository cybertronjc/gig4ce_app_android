package com.gigforce.app.modules.gigerVerfication

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigerVerification3rdPartyStatusRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String =
        COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "3rdparty_verification"
    }
}