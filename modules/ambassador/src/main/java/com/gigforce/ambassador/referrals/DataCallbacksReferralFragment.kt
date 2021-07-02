package com.gigforce.ambassador.referrals

import com.gigforce.core.datamodels.profile.ProfileData
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