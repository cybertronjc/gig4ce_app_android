package com.gigforce.app.modules.referrals

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.FieldPath

class ModelReferralFragmentViewModel : BaseFirestoreDBRepository(), DataCallbacksReferralFragment {


    override fun getCollectionName(): String {
        return "Profiles"
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