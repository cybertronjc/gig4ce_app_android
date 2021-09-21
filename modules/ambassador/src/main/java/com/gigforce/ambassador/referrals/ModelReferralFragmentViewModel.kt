package com.gigforce.ambassador.referrals

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.extensions.getOrThrow
import com.google.firebase.firestore.FieldPath

class ModelReferralFragmentViewModel : BaseFirestoreDBRepository(), DataCallbacksReferralFragment {


    override fun getCollectionName(): String {
        return "Profiles"
    }

    override suspend fun getReferredPeople(
            profileIDs: List<String>
    ): List<ProfileData> {
        val profiles = mutableListOf<ProfileData>()

        profileIDs.chunked(10).forEach {
            val getProfiledQuery = getCollectionReference().whereIn(FieldPath.documentId(), it).getOrThrow()
            profiles.addAll(getProfiledQuery.toObjects(ProfileData::class.java))
        }

        return profiles
    }

    override fun getReferredPeople(
            responseCallbacks: DataCallbacksReferralFragment.ResponseCallbacks,
            profileIDs: List<String>
    ) {


        getCollectionReference().whereIn(FieldPath.documentId(), profileIDs)
                .addSnapshotListener { snapshot, err ->
                    responseCallbacks.referredPeopleResponse(snapshot, err)
                }
    }


}