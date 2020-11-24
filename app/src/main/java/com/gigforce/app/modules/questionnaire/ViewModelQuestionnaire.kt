package com.gigforce.app.modules.questionnaire

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.questionnaire.models.QuestionnaireResponse
import com.gigforce.app.utils.StringConstants

class ViewModelQuestionnaire(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var initialized: Boolean = false
    val questionnaireRepository = QuestionnaireRepository()

    private val _observableQuestionnaireResponse: MutableLiveData<QuestionnaireResponse>
        get() = savedStateHandle.getLiveData(
                StringConstants.SAVED_STATE.value,
                QuestionnaireResponse()
        )
    val observableQuestionnaireResponse: MutableLiveData<QuestionnaireResponse> = _observableQuestionnaireResponse

    fun getQuestionnaire() {
        questionnaireRepository.getCollectionReference().whereEqualTo("type", "questionnary").addSnapshotListener { success, error ->
            if (error == null) {
                var toObject = success?.toObjects(QuestionnaireResponse::class.java)?.get(0)
                _observableQuestionnaireResponse.value = toObject
                savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
            }
        }
    }

}