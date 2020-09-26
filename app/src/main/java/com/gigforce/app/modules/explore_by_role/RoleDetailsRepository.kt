package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

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
}