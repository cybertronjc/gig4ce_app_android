package com.gigforce.app.modules.gigPage

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.gigPage.models.GigAttendance

class GigsRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())
    fun markAttendance(markAttendance : GigAttendance,gigId:String) {
        getCollectionReference().document(gigId).update(markAttendance.tableName, markAttendance)
    }

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}