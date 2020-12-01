package com.gigforce.app.modules.landingscreen

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
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

    override fun getWorkOrder(responseCallbacks: LandingScreenCallbacks.ResponseCallbacks) {
        db.collection("Job_Profiles").limit(1).addSnapshotListener { success, error ->
            responseCallbacks.getWorkOrderResponse(success, error)
        }
    }
}