package com.gigforce.app.modules.landingscreen

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

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
        collection.addSnapshotListener { success, error ->
            run {
                responseCallbacks.getRolesResponse(success, error)
            }
        }

    }
}