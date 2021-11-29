package com.gigforce.learning.assessment.questionnaire

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.client_activation.Cities
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.datamodels.client_activation.States
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.learning.assessment.questionnaire.models.GfUsers
import com.gigforce.learning.assessment.questionnaire.models.QuestionnaireResponse
import com.gigforce.learning.assessment.questionnaire.models.Questions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

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

    private val _observableStates = MutableLiveData<MutableList<States>>()

    val observableStates: MutableLiveData<MutableList<States>> =
            _observableStates


    private val _observableCities = MutableLiveData<MutableList<Cities>>()

    val observableCities: MutableLiveData<MutableList<Cities>> =
            _observableCities

    private val _observableAllCities = MutableLiveData<MutableList<GfUsers>>()

    val observableAllCities: MutableLiveData<MutableList<GfUsers>> =
            _observableAllCities


    fun getQuestionnaire(jobProfileID: String) {
        questionnaireRepository.getCollectionReference().whereEqualTo("type", "questionnaire")
            .whereEqualTo("jobProfileId", jobProfileID).addSnapshotListener { success, error ->
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
        mJobProfileId: String,
        title: String,
        type: String,
        questions: List<Questions>?
    ) {
        var listener: ListenerRegistration? = null
        listener = questionnaireRepository.db.collection("JP_Applications")
                .whereEqualTo("jpid", mJobProfileId)
                .whereEqualTo("gigerId", questionnaireRepository.getUID())
                .addSnapshotListener { jp_application, _ ->
                    listener?.remove()

                    listener = questionnaireRepository.db.collection("JP_Applications")
                            .document(jp_application?.documents!![0].id).collection("Submissions")
                            .whereEqualTo("stepId", mJobProfileId).whereEqualTo("title", title)
                            .whereEqualTo("type", type).addSnapshotListener { questionnaire, err_ ->
                                listener?.remove()
                                if (questionnaire?.documents.isNullOrEmpty()) {
                                    questionnaireRepository.db.collection("JP_Applications")
                                            .document(jp_application.documents[0].id).collection("Submissions")
                                            .document().set(
                                                    mapOf(
                                                            "title" to title,
                                                            "type" to type,
                                                            "stepId" to mJobProfileId,
                                                            "answers" to questions?.map {
                                                                mapOf(
                                                                        "question" to it.question,
                                                                        "selectedAnswer" to it.selectedAnswer,
                                                                        "selectedDate" to it.selectedDate,
                                                                        "selectedState" to it.selectedState,
                                                                        "selectedCity" to it.selectedCity,
                                                                        "answer" to it.answer,
                                                                        "type" to it.type

                                                                )
                                                            },
                                                            "submissionDate" to Date(),
                                                            "updatedAt" to Timestamp.now(),
                                                            "updatedBy" to FirebaseAuthStateListener.getInstance()
                                                                .getCurrentSignInUserInfoOrThrow().uid

                                                    )
                                            ).addOnCompleteListener { complete ->
                                                run {

                                                    if (complete.isSuccessful) {
                                                        val jpApplication =
                                                                jp_application.toObjects(JpApplication::class.java)[0]
                                                        jpApplication.application.forEach { draft ->
                                                            if (draft.title == title) {
                                                                draft.isDone = true
                                                            }
                                                        }
                                                        var map = mapOf("application" to jpApplication.application, "updatedAt" to Timestamp.now(), "updatedBy" to FirebaseAuthStateListener.getInstance()
                                                            .getCurrentSignInUserInfoOrThrow().uid)
                                                        questionnaireRepository.db.collection("JP_Applications")
                                                                .document(jp_application.documents[0].id)
                                                                .update(map)
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
                                            .document(jp_application.documents[0].id)
                                            .collection("Submissions")
                                            .document(questionnaire?.documents?.get(0)?.id!!)
                                            .update(
                                                    mapOf(
                                                            "answers" to questions?.map {
                                                                mapOf(
                                                                        "question" to it.question,
                                                                        "selectedAnswer" to it.selectedAnswer,
                                                                        "selectedDate" to it.selectedDate,
                                                                        "selectedState" to it.selectedState,
                                                                        "selectedCity" to it.selectedCity,
                                                                        "answer" to it.answer,
                                                                        "type" to it.type

                                                                )
                                                            },
                                                            "submissionDate" to Date()
                                                    )
                                            )
                                            .addOnCompleteListener { complete ->
                                                if (complete.isSuccessful) {
                                                    val jpApplication =
                                                            jp_application.toObjects(JpApplication::class.java)[0]
                                                    jpApplication.application.forEach { draft ->
                                                        if (draft.title == title) {
                                                            draft.isDone = true
                                                        }
                                                    }
                                                    var map = mapOf("application" to jpApplication.application, "updatedAt" to Timestamp.now(), "updatedBy" to FirebaseAuthStateListener.getInstance()
                                                        .getCurrentSignInUserInfoOrThrow().uid)
                                                    questionnaireRepository.db.collection("JP_Applications")
                                                            .document(jp_application.documents[0].id)
                                                            .update(map)
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

    fun getState() = viewModelScope.launch {

        _observableStates.value = getStatesFromDb()
    }

    suspend fun getStatesFromDb(): MutableList<States> {
        try {
            val await = questionnaireRepository.db.collection("Mst_States").get().await()
            if (await.documents.isNullOrEmpty()) {
                return mutableListOf()
            }
            val toObjects = await.toObjects(States::class.java)
            for (i in 0 until await.documents.size) {
                toObjects[i].id = await.documents[i].id

            }
            return toObjects
        } catch (e: Exception) {
            _observableError.value = e.message
            return mutableListOf()
        }

    }


    fun getCities(states: States) = viewModelScope.launch {

        _observableCities.value = getCitiesFromDb(states)!!
    }

    suspend fun getCitiesFromDb(states: States): MutableList<Cities> {
        try {
            val await = questionnaireRepository.db.collection("Mst_Cities")
                    .whereEqualTo("state_code", states.id).get().await()
            if (await.documents.isNullOrEmpty()) {
                return mutableListOf()
            }

            return await.toObjects(Cities::class.java)
        } catch (e: Exception) {
            _observableError.value = e.message
            return mutableListOf()
        }


    }

    fun getAllCities() = viewModelScope.launch {
        _observableAllCities.value = getAllCitiesFromDb()

    }


    suspend fun getAllCitiesFromDb(): MutableList<GfUsers> {
        try {
            val await = questionnaireRepository.db.collection("GF_Users")
                    .get().await()
            if (await.documents.isNullOrEmpty()) {
                return mutableListOf()
            }

            return await.toObjects(GfUsers::class.java)
        } catch (e: Exception) {
            _observableError.value = e.message
            return mutableListOf()
        }


    }


}