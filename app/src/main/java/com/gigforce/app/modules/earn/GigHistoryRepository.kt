package com.gigforce.app.modules.earn

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.utils.getEndOfDay

import com.gigforce.app.utils.getStartOfDay


class GigHistoryRepository : BaseFirestoreDBRepository(), DataCallbacks {
    override fun getOnGoingGigs(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .whereGreaterThanOrEqualTo("startDateTime", getStartOfDay())
            .whereLessThanOrEqualTo("startDateTime", getEndOfDay())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.onGoingGigsResponse(querySnapshot, firebaseFirestoreException);
            }
    }


    override fun getPastGigs(responseCallbacks: DataCallbacks.ResponseCallbacks, page: Int) {
        getCollectionReference().whereEqualTo("gigerId", getUID()).orderBy("startDateTime").startAfter(page * 10)
            .limit(10)
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