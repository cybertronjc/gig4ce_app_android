package com.gigforce.app.modules.earn

import com.gigforce.app.modules.gigPage.models.Gig
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface DataCallbacks {
    fun getOnGoingGigs(responseCallbacks: ResponseCallbacks)
    fun getPastGigs(responseCallbacks: ResponseCallbacks, page: Int)
    fun getUpComingGigs(responseCallbacks: ResponseCallbacks, page: Int)
    fun checkGigsCount(responseCallbacks: ResponseCallbacks)

    open interface ResponseCallbacks {
        fun onGoingGigsResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun pastGigsResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun upcomingGigsResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
        fun gigsCountResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
    }
}