package com.gigforce.app.modules.explore_by_role

import com.gigforce.core.fb.BaseFirestoreDBRepository

class RepositoryAddContact : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Profiles"
    }
}