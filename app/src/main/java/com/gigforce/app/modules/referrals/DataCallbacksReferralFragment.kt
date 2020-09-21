package com.gigforce.app.modules.referrals

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface DataCallbacksReferralFragment {
    fun getReferredPeople(
        responseCallbacks: ResponseCallbacks,
        profileIDs: List<String>
    )

    interface ResponseCallbacks {

        fun referredPeopleResponse(
            querySnapshot: QuerySnapshot?,
            error: FirebaseFirestoreException?
        )

    }

}