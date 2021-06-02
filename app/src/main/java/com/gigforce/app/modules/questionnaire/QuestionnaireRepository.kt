package com.gigforce.app.modules.questionnaire

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class QuestionnaireRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Settings";
    }

}