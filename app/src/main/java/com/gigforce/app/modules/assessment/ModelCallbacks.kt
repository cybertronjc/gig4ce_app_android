package com.gigforce.app.modules.assessment

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

interface ModelCallbacks {

    fun getQuestionaire(callbacks: ModelResponseCallbacks)

    interface ModelResponseCallbacks {

        fun QuestionairreSuccess(
            value: QuerySnapshot?,
            e: FirebaseFirestoreException?
        )
    }
}