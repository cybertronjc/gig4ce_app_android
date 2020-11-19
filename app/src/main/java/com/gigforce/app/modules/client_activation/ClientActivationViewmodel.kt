package com.gigforce.app.modules.client_activation

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.landingscreen.models.WorkOrder
import com.gigforce.app.utils.SingleLiveEvent

class ClientActivationViewmodel : ViewModel() {

    val clientActivationRepository = ClientActivationRepository()

    private val _observableWorkOrder: SingleLiveEvent<WorkOrder> by lazy {
        SingleLiveEvent<WorkOrder>();
    }
    val observableWorkOrder: SingleLiveEvent<WorkOrder> get() = _observableWorkOrder

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    fun getWorkOrder(docID: String) {
        clientActivationRepository.getCollectionReference().document(docID)
            .addSnapshotListener { success, error ->
                run {
                    if (error != null) {
                        _observableError.value = error.message
                    } else {
                        _observableWorkOrder.value = success?.toObject(WorkOrder::class.java)
                    }

                }
            }

    }


}