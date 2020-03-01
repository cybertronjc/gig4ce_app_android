package com.gigforce.app.modules.onboarding.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.app.modules.onboarding.models.OnboardingChatLog
import com.gigforce.app.modules.onboarding.models.Profile
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class OnboardingController(val userid:String,
                           val profileManager: ProfileManager) {

    private lateinit var collectionReference: CollectionReference

    private val _logs: MutableLiveData<List<OnboardingChatLog?>> = MutableLiveData<List<OnboardingChatLog?>>(listOf())
    val logs: LiveData<List<OnboardingChatLog?>>
        get() = _logs

    private val _activeQuestion: MutableLiveData<OnboardingChatLog> = MutableLiveData()
    val activeQuestion:LiveData<OnboardingChatLog>
        get() = _activeQuestion

    init {
        collectionReference = FirebaseFirestore.getInstance()
            .collection("OnboardingChatLogs")

        addLogsListener(userid)
    }

    private fun addLogsListener(userid: String) {
        FirebaseFirestore.getInstance()
            .collection("OnboardingChatLogs")
            .whereEqualTo("userid", userid)
            .addSnapshotListener { snapshot, exception ->
                _logs.postValue(snapshot?.documents?.map { item -> item.toObject(OnboardingChatLog::class.java)}?.toList())
                setActiveQuestionAtFirst()
            }
    }

    private fun setActiveQuestionAtFirst() {
        // last point of the log, of flow == "in"
        // should update everytime the log is refreshed.
        // todo: correct the logic here
//        _activeQuestion.postValue(_logs.value!!.get(_logs.value!!.size))
        _activeQuestion.postValue(OnboardingChatLog(
            "autoid",
            "in","Can I know your name please?", "text", "name",
            userid, "giger"
        ))
    }

    fun processResponse(response: Any) {

        //todo: process response here, and update profile here

        // after the response is processed, next question should be created
        createNextQuestion()
    }

    fun createNextQuestion() {
        val profile: Profile = profileManager.profileDoc.value!!

        //todo: complete this
        if(!profile.hasName()) {
            addNewQuestionInLog(
                OnboardingChatLog(
                "autoid",
                "in","Can I know your name please?", "text", "name",
                    userid, "giger"
            ))
        }else{

        }
    }

    private fun addNewQuestionInLog(log: OnboardingChatLog){
        //todo: insert log into Firestore
    }
}