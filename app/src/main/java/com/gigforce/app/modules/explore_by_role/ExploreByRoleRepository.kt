package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

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
}