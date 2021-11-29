package com.gigforce.learning.assessment

import com.gigforce.core.StringConstants
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.learning.assessment.models.AssementQuestionsReponse
import com.google.firebase.Timestamp
import java.util.*

class ModelAssessmentFragment : BaseFirestoreDBRepository(), ModelCallbacks {
    override fun getCollectionName(): String {
        return "Course_blocks"
    }

    override fun getQuestionaire(   lessonId : String,callbacks: ModelCallbacks.ModelResponseCallbacks) {
        getCollectionReference().whereEqualTo("topictype", "assessment")
            .whereEqualTo("lesson_id", lessonId)
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
            },
            "updatedAt" to Timestamp.now(),
            "updatedBy" to FirebaseAuthStateListener.getInstance()
                .getCurrentSignInUserInfoOrThrow().uid,
            "createdAt" to Timestamp.now()

        )).addOnCompleteListener {
            if (it.isSuccessful) {
                callbacks.submitAnswerSuccess()
            } else {
                callbacks.submitAnswerFailure(it?.exception?.message ?: "")
            }
        }
    }
}