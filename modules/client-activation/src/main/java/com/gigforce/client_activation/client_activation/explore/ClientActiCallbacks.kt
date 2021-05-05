package com.gigforce.client_activation.client_activation.explore

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface ClientActiCallbacks {

    fun getJobProfiles(responseCallbacks: ResponseCallbacks)

    interface ResponseCallbacks{

        fun getJobProfilesResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)
    }
}