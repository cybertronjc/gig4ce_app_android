package com.gigforce.client_activation.client_activation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.models.*
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.SingleLiveEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DocSubSchedulerViewModel : ViewModel() {
    val repository: DocSubSchedulerRepository = DocSubSchedulerRepository()
    private val _observableJpApplication: MutableLiveData<DrivingCertificate> = MutableLiveData()
    val observableJpApplication: MutableLiveData<DrivingCertificate> = _observableJpApplication
    private val _observableIsCheckoutDone: MutableLiveData<Boolean> = MutableLiveData()
    val observableIsCheckoutDone: MutableLiveData<Boolean> = _observableIsCheckoutDone

    private val _observableQuestionnairDocument: MutableLiveData<QustionSubmissionsDocModel> = MutableLiveData()
    var observableQuestionnairDocument = _observableQuestionnairDocument

    fun getApplication(mJobProfileId: String, type: String, title: String) = viewModelScope.launch {
        val model = getJPApplication(mJobProfileId, type, title)
        _observableJpApplication.value = model

    }

    suspend fun getJPApplication(
            jobProfileID: String,
            type: String,
            title: String
    ): DrivingCertificate? {
        try {


            val items =
                    repository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileID)
                            .whereEqualTo("gigerId", repository.getUID()).get()
                            .await()
            if (items.documents.isNullOrEmpty()) {
                return null
            }

            val qustionSubmission = repository.getCollectionReference().document(items.documents[0].id)
                    .collection("Submissions").whereEqualTo("type", "questionnaire").get().await()
            if (qustionSubmission.documents.isNotEmpty()) {
                _observableQuestionnairDocument.value = qustionSubmission.toObjects(QustionSubmissionsDocModel::class.java)[0]
                Log.e("data", "working")
            }

            val toObject = items.documents[0].toObject(JpApplication::class.java)
            _observableIsCheckoutDone.value = toObject?.activation?.all { it.isDone }
            val submissions = repository.getCollectionReference().document(items.documents[0].id)
                    .collection("Submissions").whereEqualTo("stepId", jobProfileID).whereEqualTo(
                            "title", title
                    ).whereEqualTo("type", type).get().await()
            if (submissions.documents.isNullOrEmpty()) {
                return null
            }



            return submissions.toObjects(CheckoutGigforceOffice::class.java)[0].certificate
        } catch (e: Exception) {
            observableError.value = e.message
            return null
        }

    }

    private val _observablePartnerSchool: MutableLiveData<PartnerSchool> = MutableLiveData()
    val observablePartnerSchool: MutableLiveData<PartnerSchool> = _observablePartnerSchool

    private val _observableMappedUser: MutableLiveData<GFMappedUser> = MutableLiveData()
    val observableMappedUser = _observableMappedUser

    private val _observableProfile: MutableLiveData<ProfileData> = MutableLiveData()
    val observableProfile: MutableLiveData<ProfileData> = _observableProfile
    var gfmappedUserObj: GFMappedUser? = null

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    fun getPartnerSchoolDetails(type: String, jobProfileID: String) {

        repository.db.collection("JP_Settings").whereEqualTo("type", type)
                .whereEqualTo("jobProfileId", jobProfileID).limit(1)
                .addSnapshotListener { success, err ->
                    if (err == null) {
                        _observablePartnerSchool.value =
                                success?.toObjects(PartnerSchool::class.java)?.get(0)
                    } else {
                        _observableError.value = err.message

                    }
                }
    }

    fun getMappedUser(it: String) = viewModelScope.async {
        getMappedUserWithCity(it)
    }

    suspend fun getMappedUserWithCity(it: String) {
        try {
            val gfUsers = repository.db.collection("GF_Users").document(it).get().await()
            var gfmappedUser = gfUsers.toObject(GFMappedUser::class.java)
            gfmappedUserObj = gfmappedUser
            _observableMappedUser.value = gfmappedUser
        } catch (e: Exception) {

        }
    }

    fun checkIfTeamLeadersProfileExists(loginMobile: String) = viewModelScope.launch {
        getProfileAndOpenChat(loginMobile)
    }

    suspend fun getProfileAndOpenChat(loginMobile: String) {
        try {

            val profiles = repository.db.collection("Profiles").whereEqualTo("loginMobile", loginMobile).get().await()
            if (!profiles.documents.isNullOrEmpty()) {
                val toObject = profiles.documents[0].toObject(ProfileData::class.java)
                toObject?.id = profiles.documents[0].id
                _observableProfile.value = toObject
            }

        } catch (e: Exception) {

        }
    }

    fun getUid(): String {
        return repository.getUID()
    }


}