package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.*
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.StringConstants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DocSubSchedulerViewModel : ViewModel() {
    val repository: DocSubSchedulerRepository = DocSubSchedulerRepository()
    private val _observableJpApplication: MutableLiveData<DrivingCertificate> = MutableLiveData()
    val observableJpApplication: MutableLiveData<DrivingCertificate> = _observableJpApplication
    private val _observableIsCheckoutDone: MutableLiveData<Boolean> = MutableLiveData()
    val observableIsCheckoutDone: MutableLiveData<Boolean> = _observableIsCheckoutDone


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
            val toObject = items.documents[0].toObject(JpApplication::class.java)
            _observableIsCheckoutDone.value = toObject?.activation?.all { it.isDone }
            val submissions = repository.getCollectionReference().document(items.documents[0].id)
                .collection("Submissions").whereEqualTo("stepId", jobProfileID).whereEqualTo(
                    "title", title
                ).whereEqualTo("type", type).get().await()
            if (submissions.documents.isNullOrEmpty()) {
                return null
            }

            return submissions.toObjects(DrivingCertSubmission::class.java)[0].certificate
        } catch (e: Exception) {
            observableError.value = e.message
            return null
        }

    }

    private val _observablePartnerSchool: MutableLiveData<PartnerSchool> = MutableLiveData()
    val observablePartnerSchool: MutableLiveData<PartnerSchool> = _observablePartnerSchool
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


}