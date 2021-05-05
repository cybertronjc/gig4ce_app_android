package com.gigforce.client_activation.client_activation.repository

import com.gigforce.client_activation.client_activation.explore.ClientActiCallbacks
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class ClientActiExploreRepository : BaseFirestoreDBRepository(), ClientActiCallbacks {

    override fun getCollectionName(): String {
        return ""
    }

    override fun getJobProfiles(responseCallbacks: ClientActiCallbacks.ResponseCallbacks) {
        db.collection("Job_Profiles").whereEqualTo("isActive", true)
            .addSnapshotListener { success, error ->
                responseCallbacks.getJobProfilesResponse(success, error)
            }
    }


}