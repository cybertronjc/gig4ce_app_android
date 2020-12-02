package com.gigforce.app.modules.questionnaire

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.questionnaire.models.QuestionnaireResponse
import com.gigforce.app.modules.questionnaire.models.Questions
import com.google.firebase.firestore.ListenerRegistration

class ViewModelQuestionnaire(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var initialized: Boolean = false
    val questionnaireRepository = QuestionnaireRepository()

    private val _observableQuestionnaireResponse = MutableLiveData<QuestionnaireResponse>()

    val observableQuestionnaireResponse: MutableLiveData<QuestionnaireResponse> =
        _observableQuestionnaireResponse

    private val _observableAddApplicationSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val observableAddApplicationSuccess: MutableLiveData<Boolean> = _observableAddApplicationSuccess
    private val _observableError: MutableLiveData<String> = MutableLiveData()
    val observableError: MutableLiveData<String> = _observableError


    fun getQuestionnaire(workOrderID: String) {
        questionnaireRepository.getCollectionReference().whereEqualTo("type", "questionnaire")
            .whereEqualTo("jobProfileId", workOrderID).addSnapshotListener { success, error ->
                if (error == null) {
                    if (!success?.documents.isNullOrEmpty()) {
                        val toObject = success?.toObjects(QuestionnaireResponse::class.java)?.get(0)
                        _observableQuestionnaireResponse.value = toObject
//                        savedStateHandle.set(StringConstants.SAVED_STATE.value, toObject)
                    }

                }
            }
    }

    fun addQuestionnaire(
        mWordOrderID: String,
        title: String,
        type: String,
        questions: List<Questions>?
    ) {
        var listener: ListenerRegistration? = null
        listener = questionnaireRepository.db.collection("JP_Applications")
            .whereEqualTo("jpid", mWordOrderID)
            .whereEqualTo("gigerId", questionnaireRepository.getUID())
            .addSnapshotListener { jp_application, err ->
                listener?.remove()

                listener = questionnaireRepository.db.collection("JP_Applications")
                    .document(jp_application?.documents!![0].id).collection("submissions")
                    .whereEqualTo("stepId", mWordOrderID).whereEqualTo("title", title)
                    .whereEqualTo("type", type).addSnapshotListener { questionnaire, err ->
                        listener?.remove()
                        if (questionnaire?.documents.isNullOrEmpty()) {
                            questionnaireRepository.db.collection("JP_Applications")
                                .document(jp_application.documents[0].id).collection("submissions")
                                .document().set(
                                    mapOf(
                                        "title" to title,
                                        "type" to type,
                                        "stepId" to mWordOrderID,
                                        "answers" to questions

                                    )
                                ).addOnCompleteListener { complete ->
                                    run {

                                        if (complete.isSuccessful) {
                                            val jpApplication =
                                                jp_application.toObjects(JpApplication::class.java)[0]
                                            jpApplication.draft.forEach { draft ->
                                                if (draft.title == title) {
                                                    draft.isDone = true
                                                }
                                            }
                                            questionnaireRepository.db.collection("JP_Applications")
                                                .document(jp_application.documents[0].id)
                                                .update("draft", jpApplication.draft)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        _observableAddApplicationSuccess.value =
                                                            true
                                                    }
                                                }
                                        }
                                    }
                                }
                        } else {
                            questionnaireRepository.db.collection("JP_Applications")
                                .document(jp_application?.documents!![0].id)
                                .collection("submissions")
                                .document(questionnaire?.documents?.get(0)?.id!!)
                                .update("answers", questions)
                                .addOnCompleteListener { complete ->
                                    if (complete.isSuccessful) {
                                        val jpApplication =
                                            jp_application.toObjects(JpApplication::class.java)[0]
                                        jpApplication.draft.forEach { draft ->
                                            if (draft.title == title) {
                                                draft.isDone = true
                                            }
                                        }
                                        questionnaireRepository.db.collection("JP_Applications")
                                            .document(jp_application.documents[0].id)
                                            .update("draft", jpApplication.draft)
                                            .addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    _observableAddApplicationSuccess.value =
                                                        true
                                                }
                                            }
                                    }
                                }
                        }
                    }


            }


    }




}