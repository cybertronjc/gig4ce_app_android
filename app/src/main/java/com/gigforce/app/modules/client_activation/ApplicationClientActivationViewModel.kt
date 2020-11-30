package com.gigforce.app.modules.client_activation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.JpDraft
import com.gigforce.app.modules.client_activation.models.WorkOrderDependency
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ApplicationClientActivationViewModel : ViewModel() {
    val repository = ApplicationClientActivationRepository()

    private val _observableWorkOrderDependency: SingleLiveEvent<WorkOrderDependency> by lazy {
        SingleLiveEvent<WorkOrderDependency>();
    }
    val observableWorkOrderDependency: SingleLiveEvent<WorkOrderDependency> get() = _observableWorkOrderDependency

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableApplicationStatus: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableApplicationStatus: SingleLiveEvent<Boolean> get() = _observableApplicationStatus


    private val _observableVerification: SingleLiveEvent<VerificationBaseModel> by lazy {
        SingleLiveEvent<VerificationBaseModel>();
    }
    val observableVerification: SingleLiveEvent<VerificationBaseModel> get() = _observableVerification

    private val _observableJpApplication: MutableLiveData<JpApplication> = MutableLiveData()
    val observableJpApplication: MutableLiveData<JpApplication> = _observableJpApplication

    fun getWorkOrderDependency(workOrderId: String) {
        repository.getCollectionReference().whereEqualTo("type", "dependency")
                .whereEqualTo("workOrderId", workOrderId).addSnapshotListener { success, error ->
                    if (error != null) {
                        observableError.value = error.message

                    } else {
                        if (!success?.documents.isNullOrEmpty()) {
                            _observableWorkOrderDependency.value =
                                    success?.toObjects(WorkOrderDependency::class.java)!![0]
                        }

                    }

                }
    }


    fun getVerification() {
        repository.db.collection("Verification").document(repository.getUID())
                .addSnapshotListener { element, err ->
                    run {
                        if (err == null) {
                            _observableVerification.value =
                                    element?.toObject(VerificationBaseModel::class.java);
                        } else {
                            _observableError.value = err.message
                        }
                    }
                }
    }

    fun apply(mWorkOrderID: String, draft: MutableList<JpDraft>) = viewModelScope.launch {


        val model = getJPApplication(mWorkOrderID)


        model.stepDone = 2
        model.draft = draft
        if (model.id.isEmpty()) {
            repository.db.collection("JP_Applications").document().set(model).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableApplicationStatus.value = true
                }
            }
        } else {
            repository.db.collection("JP_Applications").document(model.id).set(model).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableApplicationStatus.value = true
                }
            }
        }


    }

    fun getApplication(workOrderId: String) {
        repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderId).whereEqualTo("gigerId", repository.getUID()).addSnapshotListener { success, err ->
            run {
                Log.e("check", err?.message ?: "")
                if (err == null) {
                    if (!success?.documents.isNullOrEmpty()) {
                        observableJpApplication.value = success?.toObjects(JpApplication::class.java)?.get(0)
                    }
                } else {
                    observableError.value = err.message
                }
            }
        }
    }


    suspend fun getJPApplication(workOrderID: String): JpApplication {
        val items = repository.getCollectionReference().whereEqualTo("jpid", workOrderID).whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        if (items.documents.isNullOrEmpty()) {
            return JpApplication(JPId = workOrderID, gigerId = repository.getUID())
        }
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
        return toObject
    }


}
