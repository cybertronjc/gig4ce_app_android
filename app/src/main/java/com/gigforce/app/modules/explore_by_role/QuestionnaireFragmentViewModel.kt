package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.explore_by_role.models.QuestionnaireResponse
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp

class QuestionnaireFragmentViewModel : ViewModel() {

    val questionaireRepo = QuestionnaireFragmentRepo()

    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess


    fun addQuestionnaire(answers: List<QuestionnaireResponse>) {
        val map = mapOf("questionnaire" to answers, "updatedAt" to Timestamp.now(), "updatedBy" to questionaireRepo.getUID())
        questionaireRepo.getCollectionReference().document(questionaireRepo.getUID())
            .update(map).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }
}