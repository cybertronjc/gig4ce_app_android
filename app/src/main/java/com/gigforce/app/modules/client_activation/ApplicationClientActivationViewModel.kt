package com.gigforce.app.modules.client_activation

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.WorkOrderDependency
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.utils.SingleLiveEvent

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

    fun apply(jpApplication: JpApplication) {
        repository.db.collection("JP_Applications").document(jpApplication.JPId).set(
                jpApplication
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                _observableApplicationStatus.value = true
            } else {
                _observableError.value = it.exception?.message ?: ""
            }

        }
    }


}
