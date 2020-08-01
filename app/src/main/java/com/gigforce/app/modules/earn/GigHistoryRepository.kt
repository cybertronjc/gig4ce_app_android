package com.gigforce.app.modules.earn

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigHistoryRepository : BaseFirestoreDBRepository(), DataCallbacks {
    override fun getOnGoingGigs(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
//            .whereGreaterThanOrEqualTo("startDateTime", getStartOfDayInMillis().toString())
//            .whereLessThanOrEqualTo("startDateTime", getEndOfDayInMillis().toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.onGoingGigsResponse(querySnapshot, firebaseFirestoreException);
            }
    }


    override fun getPastGigs(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
//            .whereGreaterThanOrEqualTo("startDateTime", getStartOfDayInMillis().toString())
//            .whereLessThanOrEqualTo("startDateTime", getEndOfDayInMillis().toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.pastGigsResponse(querySnapshot, firebaseFirestoreException);
            }
    }

    override fun getUpComingGigs(responseCallbacks: DataCallbacks.ResponseCallbacks) {
    }

    override fun getCollectionName() = COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}