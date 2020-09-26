package com.gigforce.app.modules.landingscreen

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.landingscreen.models.Role
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
//                val role: Role =
//                    success?.toObjects(Role::class.java)?.get(0)!!
//                role.role_title="DELIVERY EXECUTIVE"
//                db.collection("Roles").document().set(role)
            }
        }

    }
}