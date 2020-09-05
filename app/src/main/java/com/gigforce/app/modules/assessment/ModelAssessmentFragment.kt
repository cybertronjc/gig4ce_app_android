package com.gigforce.app.modules.assessment

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.assessment.models.AssementQuestionsReponse
import java.util.*

class ModelAssessmentFragment : BaseFirestoreDBRepository(), ModelCallbacks {
    override fun getCollectionName(): String {
        return "Course_blocks"
    }

    override fun getQuestionaire(callbacks: ModelCallbacks.ModelResponseCallbacks) {
        getCollectionReference().whereEqualTo("topictype", "assessment")
            .whereEqualTo("lesson_id", "oyFhCjdddc6zwIswClds")
            .addSnapshotListener { value, error ->
                run {
                    callbacks.QuestionairreSuccess(value, error)

                }
            }
    }

    override fun submitAnswers(
        profileID: String,
        assessmentResponse: AssementQuestionsReponse,
        callbacks: ModelCallbacks.ModelResponseCallbacks
    ) {
        db.collection("assessment_submissions").document().set(mapOf(
            "active" to true,
            "source" to "app_android",
            "profile_id" to profileID,
            "inserted_at" to Date(),
            "lesson_id" to assessmentResponse.lesson_id,
            "time_taken_in_millis" to assessmentResponse.timeTakenInMillis,
            "answers" to assessmentResponse.assessment?.map {
                var is_correct = false
                it.options?.forEach { elem ->
                    run {
                        if (elem.is_answer == true && elem.selectedAnswer == true) {
                            is_correct = true
                        }
                    }

                }
                mapOf("is_correct" to is_correct)
            }
        )).addOnCompleteListener {
            if (it.isSuccessful) {
                callbacks.submitAnswerSuccess()
            } else {
                callbacks.submitAnswerFailure(it?.exception?.message ?: "")
            }
        }
    }
}