package com.gigforce.app.modules.explore_by_role

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class QuestionnaireFragmentRepo : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Profiles"
    }

}