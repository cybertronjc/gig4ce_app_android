package com.gigforce.giger_gigs.gighistory

import com.gigforce.core.datamodels.gigpage.Gig
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

interface DataCallbacks {
    suspend fun getOnGoingGigs(
        responseCallbacks: ResponseCallbacks,
        initialLoading: Boolean
    )

    suspend fun getPastGigs(
        responseCallbacks: ResponseCallbacks,
        offset: Long,
        limit: Long
    )

    suspend fun getUpComingGigs(
        responseCallbacks: ResponseCallbacks,
        offset: Long,
        limit: Long
    )

    fun observeDocumentChanges(responseCallbacks: ResponseCallbacks)
    fun checkGigsCount(responseCallbacks: ResponseCallbacks)


    interface ResponseCallbacks {
        fun onGoingGigsResponse(
            gigs: List<Gig>?,
            error: Exception?,
            initialLoading: Boolean
        )

        fun pastGigsResponse(
            gigs : List<Gig>?,
            error: Exception?
        )

        fun upcomingGigsResponse(
            gigs : List<Gig>?,
            error: Exception?
        )

        fun gigsCountResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun docChange(docChangeType: DocumentChange.Type, change: DocumentChange)

    }
}