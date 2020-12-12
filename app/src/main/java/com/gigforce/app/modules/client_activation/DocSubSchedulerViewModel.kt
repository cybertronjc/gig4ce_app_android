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


    fun getApplication(mWorkOrderID: String, type: String, title: String) = viewModelScope.launch {
        val model = getJPApplication(mWorkOrderID, type, title)
        _observableJpApplication.value = model

    }

    suspend fun getJPApplication(
        workOrderID: String,
        type: String,
        title: String
    ): DrivingCertificate? {
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID)
            .whereEqualTo("gigerId", repository.getUID()).get()
            .await()
        val submissions = repository.getCollectionReference().document(items.documents[0].id)
            .collection("submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                "title", title
            ).whereEqualTo("type", type).get().await()
        if (submissions.documents.isNullOrEmpty()) {
            return null
        }

        return submissions.toObjects(DrivingCertSubmission::class.java)[0].certificate
    }

    private val _observablePartnerSchool: MutableLiveData<PartnerSchool> = MutableLiveData()
    val observablePartnerSchool: MutableLiveData<PartnerSchool> = _observablePartnerSchool
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    fun getPartnerSchoolDetails(type: String, workOrderId: String) {
        repository.db.collection("JP_Settings").whereEqualTo("type", type)
            .whereEqualTo("jobProfileId", workOrderId).limit(1)
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