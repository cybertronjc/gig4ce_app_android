package com.gigforce.app.modules.assessment

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class ModelAssessmentFragment : BaseFirestoreDBRepository(), ModelCallbacks {
    override fun getCollectionName(): String {
        return "Course_blocks"
    }

    override fun getQuestionaire(callbacks: ModelCallbacks.ModelResponseCallbacks) {
        getCollectionReference().whereEqualTo("type", "assessment").whereEqualTo("lesson_id","8W5QVSJXQV3zZG9u3afo")
            .addSnapshotListener { value, error ->
                run {
                    callbacks.QuestionairreSuccess(value, error)

                }
            }
    }
}