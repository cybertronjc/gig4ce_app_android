package com.gigforce.app.modules.earn.gighistory

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.utils.getEndOfDay
import com.gigforce.app.utils.getStartOfDay
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query


class GigHistoryRepository : BaseFirestoreDBRepository(), DataCallbacks {

    var listener: ListenerRegistration? = null
    override fun getOnGoingGigs(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        listener = getCollectionReference().whereEqualTo("gigerId", getUID())
            .whereGreaterThanOrEqualTo("startDateTime", getStartOfDay())
            .whereLessThanOrEqualTo("startDateTime", getEndOfDay())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.onGoingGigsResponse(querySnapshot, firebaseFirestoreException);
            }
    }


    override fun getPastGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        lastVisible: DocumentSnapshot?,
        limit: Long
    ) {
        val gigQuery =
            if (lastVisible != null) getCollectionReference().whereEqualTo("gigerId", getUID())
                .whereLessThan("startDateTime", getStartOfDay())
                .orderBy("startDateTime", Query.Direction.DESCENDING).startAfter(lastVisible)
                .limit(limit)
            else
                getCollectionReference().whereEqualTo("gigerId", getUID())
                    .whereLessThan("startDateTime", getStartOfDay())
                    .orderBy("startDateTime", Query.Direction.DESCENDING)

                    .limit(limit)
        listener = gigQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            responseCallbacks.pastGigsResponse(querySnapshot, firebaseFirestoreException);

        }

    }


    override fun getUpComingGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        lastVisible: DocumentSnapshot?,
        limit: Long
    ) {
        val gigQuery =
            if (lastVisible != null) getCollectionReference().whereEqualTo("gigerId", getUID())
                .whereGreaterThan("startDateTime", getEndOfDay())
                .orderBy("startDateTime", Query.Direction.ASCENDING).startAfter(lastVisible)
                .limit(limit)
            else
                getCollectionReference().whereEqualTo("gigerId", getUID())
                    .whereGreaterThan("startDateTime", getEndOfDay())
                    .orderBy("startDateTime", Query.Direction.ASCENDING)
                    .limit(limit)

        listener = gigQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            responseCallbacks.upcomingGigsResponse(querySnapshot, firebaseFirestoreException);

        }
    }

    override fun removeListener() {
        listener?.remove()
    }

    override fun observeDocumentChanges(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {

                        DocumentChange.Type.ADDED -> {
                            responseCallbacks.docChange(DocumentChange.Type.ADDED, dc)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            responseCallbacks.docChange(DocumentChange.Type.MODIFIED, dc)
                        }
                        DocumentChange.Type.REMOVED -> {
                            responseCallbacks.docChange(DocumentChange.Type.REMOVED, dc)
                        }
                    }
                }
            }
    }

    override fun checkGigsCount(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .limit(1).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.gigsCountResponse(querySnapshot, firebaseFirestoreException);
            }

    }


    override fun getCollectionName() =
        COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Gigs"

    }
}