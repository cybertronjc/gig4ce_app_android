package com.gigforce.learning.assessment.questionnaire

import com.gigforce.core.fb.BaseFirestoreDBRepository

class QuestionnaireRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Settings";
    }

}