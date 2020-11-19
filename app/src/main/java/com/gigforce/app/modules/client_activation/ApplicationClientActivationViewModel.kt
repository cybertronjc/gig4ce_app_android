package com.gigforce.app.modules.client_activation

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.modules.landingscreen.models.WorkOrderDependency
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.SingleLiveEvent
import kotlinx.android.synthetic.main.layout_fragment_client_activation.*

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

    fun getWorkOrderDependency(workOrderId: String) {
        repository.getCollectionReference().whereEqualTo("type", "dependency")
            .whereEqualTo("work_order_id", workOrderId).addSnapshotListener { success, error ->
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




}
