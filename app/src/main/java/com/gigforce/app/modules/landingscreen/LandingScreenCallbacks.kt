package com.gigforce.app.modules.landingscreen

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface LandingScreenCallbacks {

    fun getRoles(enableLimit: Boolean, responseCallbacks: ResponseCallbacks)

    interface ResponseCallbacks {
        fun getRolesResponse(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?)


    }
}