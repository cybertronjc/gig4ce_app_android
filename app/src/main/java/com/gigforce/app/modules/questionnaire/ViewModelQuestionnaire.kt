package com.gigforce.app.modules.questionnaire

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.QuestionsSubmission
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.modules.questionnaire.models.QuestionnaireResponse
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.app.utils.StringConstants
import com.google.firebase.firestore.ListenerRegistration

class ViewModelQuestionnaire(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var initialized: Boolean = false
    val questionnaireRepository = QuestionnaireRepository()

    private val _observableQuestionnaireResponse: MutableLiveData<QuestionnaireResponse>
        get() = savedStateHandle.getLiveData(
                StringConstants.SAVED_STATE.value,
                QuestionnaireResponse()
        )
    val observableQuestionnaireResponse: MutableLiveData<QuestionnaireResponse> = _observableQuestionnaireResponse

    private val _observableAddApplicationSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val observableAddApplicationSuccess: MutableLiveData<Boolean> = _observableAddApplicationSuccess
    private val _observableError: MutableLiveData<String> = MutableLiveData()
    val observableError: MutableLiveData<String> = _observableError


    fun getQuestionnaire(workOrderID: String) {
        questionnaireRepository.getCollectionReference().whereEqualTo("type", "questionnary").whereEqualTo("workOrderId", workOrderID).addSnapshotListener { success, error ->
            if (error == null) {
                val toObject = success?.toObjects(QuestionnaireResponse::class.java)?.get(0)
                _observableQuestionnaireResponse.value = toObject
                savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
            }
        }
    }

    fun addQuestionnaire(mWordOrderID: String, list: ArrayList<Dependency>, questions: List<Questions>?) {
        var listener: ListenerRegistration? = null
        listener = questionnaireRepository.db.collection("JP_Applications").whereEqualTo("jpid", mWordOrderID).whereEqualTo("gigerId", questionnaireRepository.getUID()).addSnapshotListener { success, err ->
            listener?.remove()
            if (success?.documents.isNullOrEmpty()) {
                questionnaireRepository.db.collection("JP_Applications").document().set(JpApplication(JPId = mWordOrderID, gigerId = questionnaireRepository.getUID(),
                        questionnaireSubmission = questions?.map {
                            QuestionsSubmission(it.question, it.selectedAnswer)
                        }!!)).addOnCompleteListener {
                    if (it.isSuccessful) {
                        _observableAddApplicationSuccess.value = true
                    } else {
                        _observableError.value = it.exception?.message

                    }
                }

            } else {
                questionnaireRepository.db.collection("JP_Applications").document(success?.documents!![0].id).update("questionnaireSubmission", questions?.map {
                    QuestionsSubmission(it.question, it.selectedAnswer)
                }).addOnCompleteListener {
                    if (it.isSuccessful) {
                        _observableAddApplicationSuccess.value = true

                    } else {
                        _observableError.value = it.exception?.message
                    }
                }
            }
        }
    }

}