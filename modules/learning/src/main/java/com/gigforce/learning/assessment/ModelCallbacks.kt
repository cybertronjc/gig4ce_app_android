package com.gigforce.learning.assessment

import com.gigforce.learning.assessment.models.AssementQuestionsReponse
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface ModelCallbacks {

    fun getQuestionaire(
        lessonId : String,
        callbacks: ModelResponseCallbacks)
    fun submitAnswers(
        profileID: String,
        assessmentResponse: AssementQuestionsReponse,
        callbacks: ModelResponseCallbacks
    )


    interface ModelResponseCallbacks {

        fun QuestionairreSuccess(
            value: QuerySnapshot?,
            e: FirebaseFirestoreException?
        )

        fun submitAnswerSuccess()
        fun submitAnswerFailure(err: String)

    }
}