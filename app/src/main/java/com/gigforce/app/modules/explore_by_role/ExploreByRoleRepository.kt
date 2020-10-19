package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.profile.models.RoleInterests
import com.google.firebase.firestore.FieldValue

class ExploreByRoleRepository : BaseFirestoreDBRepository(), ExploreByRoleCallbacks {
    override fun getCollectionName(): String {
        return "Roles"
    }

    override fun getRoles(responseCallbacks: ExploreByRoleCallbacks.ResponseCallbacks) {
        getCollectionReference().addSnapshotListener { success, error ->
            run {
                responseCallbacks.getRolesResponse(success, error)
            }
        }

    }

    override fun checkIfDocsAreVerified(responseCallbacks: ExploreByRoleCallbacks.ResponseCallbacks) {
        db.collection("Verification").document(getUID()).addSnapshotListener { element, err ->
            run {
                responseCallbacks.docsVerifiedResponse(element, err)
            }
        }
    }

    override fun markAsInterest(
        roleID: String?,
        responseCallbacks: ExploreByRoleCallbacks.ResponseCallbacks
    ) {
        db.collection("Profiles").document(getUID())
            .update("role_interests", FieldValue.arrayUnion(RoleInterests(roleID)))
            .addOnCompleteListener {
                responseCallbacks.markedAsInterestSuccess(it)
            }
    }
}