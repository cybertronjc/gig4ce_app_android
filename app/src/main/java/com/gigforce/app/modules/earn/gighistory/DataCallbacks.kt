package com.gigforce.app.modules.earn.gighistory

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface DataCallbacks {
    fun getOnGoingGigs(responseCallbacks: ResponseCallbacks)

    fun getPastGigs(
        responseCallbacks: ResponseCallbacks,
        query: DocumentSnapshot?,
        limit: Long
    )

    fun getUpComingGigs(
        responseCallbacks: ResponseCallbacks,
        lastVisible: DocumentSnapshot?,
        limit: Long
    )
    fun removeListener()
    fun observeDocumentChanges(responseCallbacks: ResponseCallbacks)
    fun checkGigsCount(responseCallbacks: ResponseCallbacks)


    interface ResponseCallbacks {
        fun onGoingGigsResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun pastGigsResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun upcomingGigsResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun gigsCountResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun docChange(docChangeType: DocumentChange.Type, change: DocumentChange)

    }
}