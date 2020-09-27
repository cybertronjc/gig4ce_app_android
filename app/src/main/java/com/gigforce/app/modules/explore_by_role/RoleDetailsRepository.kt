package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.profile.models.RoleInterests
import com.google.firebase.firestore.FieldValue

class RoleDetailsRepository : BaseFirestoreDBRepository(), RoleDetailsCallbacks {
    override fun getCollectionName(): String {
        return "Roles"
    }

    override fun getRoleDetails(
        id: String?,
        responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks
    ) {
        getCollectionReference().document(id!!).addSnapshotListener { success, error ->
            run {
                responseCallbacks.getRoleDetailsResponse(success, error)
            }
        }
    }

    override fun markAsInterest(roleID: String?,
                                responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks) {
        db.collection("Profiles").document(getUID())
            .update("role_interests", FieldValue.arrayUnion(RoleInterests(roleID))).addOnCompleteListener {
                responseCallbacks.markedAsInterestSuccess(it)
            }
    }
}