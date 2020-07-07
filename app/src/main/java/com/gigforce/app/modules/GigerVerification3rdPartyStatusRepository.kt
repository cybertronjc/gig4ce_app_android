package com.gigforce.app.modules

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigerVerification3rdPartyStatusRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "verification_3rdparty"
    }

    override fun getCustomUid(): String? {
        return "AHcFiEpfFNwbR5eARfml"
    }
}