package com.gigforce.landing_screen.landingscreen

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.ListenerRegistration

class LandingScreenRepository : BaseFirestoreDBRepository(), LandingScreenCallbacks {


    override fun getCollectionName(): String {
        return ""
    }

    override fun getRoles(
        enableLimit: Boolean,
        responseCallbacks: LandingScreenCallbacks.ResponseCallbacks
    ) {
        val collection = db.collection("Roles")
        if (enableLimit) {
            collection.limit(1)
        }
        var listener: ListenerRegistration? = null
        listener = collection.addSnapshotListener { success, error ->
//            listener?.remove()
            run {
                responseCallbacks.getRolesResponse(success, error)

            }
        }

    }

    override fun getJobProfile(responseCallbacks: LandingScreenCallbacks.ResponseCallbacks) {
        db.collection("Job_Profiles").whereEqualTo("isActive", true).limit(4)
            .addSnapshotListener { success, error ->
                responseCallbacks.getJobProfileResponse(success, error)
            }
    }
}