package com.gigforce.app.modules.client_activation

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.landingscreen.models.WorkOrderDependency
import com.gigforce.app.utils.SingleLiveEvent

class ApplicationClientActivationViewModel : ViewModel() {
    val repository = ApplicationClientActivationRepository()

    private val _observableWorkOrderDependency: SingleLiveEvent<WorkOrderDependency> by lazy {
        SingleLiveEvent<WorkOrderDependency>();
    }
    val observableWorkOrderDependency: SingleLiveEvent<WorkOrderDependency> get() = _observableWorkOrderDependency




}
