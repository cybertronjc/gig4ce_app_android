package com.gigforce.app.modules.referrals

import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface DataCallbacksReferralFragment {
    fun getReferredPeople(
            responseCallbacks: ResponseCallbacks,
            profileIDs: List<String>
    )

    suspend fun getReferredPeople(
            profileIDs: List<String>
    ): List<ProfileData>

    interface ResponseCallbacks {

        fun referredPeopleResponse(
                querySnapshot: QuerySnapshot?,
                error: FirebaseFirestoreException?
        )

    }

}