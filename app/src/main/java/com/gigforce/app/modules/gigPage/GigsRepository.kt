package com.gigforce.app.modules.gigPage

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigsRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())


    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}