package com.gigforce.app.modules.earn

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.utils.getEndOfDay
import com.gigforce.app.utils.getStartOfDay
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query


class GigHistoryRepository : BaseFirestoreDBRepository(), DataCallbacks {

    private var lastVisible: DocumentSnapshot? = null

    override fun getOnGoingGigs(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .whereGreaterThanOrEqualTo("startDateTime", getStartOfDay())
            .whereLessThanOrEqualTo("startDateTime", getEndOfDay())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.onGoingGigsResponse(querySnapshot, firebaseFirestoreException);
            }
    }


    override fun getPastGigs(responseCallbacks: DataCallbacks.ResponseCallbacks, page: Int) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .whereLessThan("startDateTime", getStartOfDay())
            .orderBy("startDateTime", Query.Direction.DESCENDING)
            .startAfter(page * 10)
            .limit(10)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.pastGigsResponse(querySnapshot, firebaseFirestoreException);
            }
    }

    override fun getUpComingGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        page: Int
    ) {
        val skipCount = page * 10
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .whereGreaterThan("startDateTime", getStartOfDay())
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .startAfter(skipCount)
            .limit(10)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.pastGigsResponse(querySnapshot, firebaseFirestoreException);
            }

    }

    override fun checkGigsCount(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .limit(1).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.gigsCountResponse(querySnapshot, firebaseFirestoreException);
            }

    }


    override fun getCollectionName() = COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}