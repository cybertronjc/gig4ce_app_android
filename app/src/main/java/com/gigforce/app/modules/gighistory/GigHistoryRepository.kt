package com.gigforce.app.modules.gighistory

import android.util.Log
import com.gigforce.core.fb.BaseFirestoreDBRepository
//import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.*


class GigHistoryRepository : BaseFirestoreDBRepository(), DataCallbacks {

    var listener: ListenerRegistration? = null
    var onGoingListener: ListenerRegistration? = null
    override fun getOnGoingGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        initialLoading: Boolean
    ) {

        onGoingListener = getCollectionReference()
            .whereEqualTo("gigerId", getUID())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                responseCallbacks.onGoingGigsResponse(
                    querySnapshot,
                    firebaseFirestoreException,
                    initialLoading
                )
            }
    }


    override fun getPastGigs(
        responseCallbacks: DataCallbacks.ResponseCallbacks,
        lastVisible: DocumentSnapshot?,
        limit: Long
    ) {

        val gigQuery =
            if (lastVisible != null) getCollectionReference()
                .whereEqualTo("gigerId", getUID())
                .whereLessThan("endDateTime", Date())
                .orderBy("endDateTime", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(limit)
            else
                getCollectionReference().whereEqualTo("gigerId", getUID())
                    .whereLessThan("endDateTime", Date())
                    .orderBy("endDateTime", Query.Direction.DESCENDING)
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

        val gigQuery = if (lastVisible != null) getCollectionReference()
            .whereEqualTo("gigerId", getUID())
            .whereGreaterThan("startDateTime", Date())
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .startAfter(lastVisible)
            .limit(limit)
        else
            getCollectionReference()
                .whereEqualTo("gigerId", getUID())
                .whereGreaterThan("startDateTime", Date())
                .orderBy("startDateTime", Query.Direction.ASCENDING)
                .limit(limit)

        Log.d("TAG", "Get Upcoming called , ${lastVisible != null}")
        listener = gigQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            Log.d("TAG", "Get Upcoming called , ${querySnapshot?.size()}")
            firebaseFirestoreException?.printStackTrace()
            responseCallbacks.upcomingGigsResponse(querySnapshot, firebaseFirestoreException);
        }
    }

    override fun removeListener() {
        listener?.remove()

    }

    override fun removeOnGoingGigsListener() {
        onGoingListener?.remove()
    }

    override fun observeDocumentChanges(responseCallbacks: DataCallbacks.ResponseCallbacks) {
        getCollectionReference().whereEqualTo("gigerId", getUID())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
//                        DocumentChange.Type.ADDED -> {
//                            responseCallbacks.docChange(DocumentChange.Type.ADDED, dc)
//                        }
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