package com.gigforce.app.modules.explore_by_role

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class AddExperienceRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Profiles"
    }

}