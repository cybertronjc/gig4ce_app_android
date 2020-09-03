package com.gigforce.app.modules.assessment

import com.gigforce.app.modules.assessment.models.AssementQuestionsReponse
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface ModelCallbacks {

    fun getQuestionaire(callbacks: ModelResponseCallbacks)
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