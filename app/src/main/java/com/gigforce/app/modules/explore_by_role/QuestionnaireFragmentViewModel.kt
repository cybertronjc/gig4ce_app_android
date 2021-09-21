package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.explore_by_role.models.QuestionnaireResponse
import com.gigforce.core.SingleLiveEvent

class QuestionnaireFragmentViewModel : ViewModel() {

    val questionaireRepo = QuestionnaireFragmentRepo()

    private val _observableSuccess: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSuccess: SingleLiveEvent<String> get() = _observableSuccess


    fun addQuestionnaire(answers: List<QuestionnaireResponse>) {
        questionaireRepo.getCollectionReference().document(questionaireRepo.getUID())
            .update("questionnaire", answers).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableSuccess.value = "true"
                } else {
                    observableSuccess.value = it.exception?.message
                }
            }
    }
}