package com.gigforce.app.modules.gigPage

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Gig"
    }
}