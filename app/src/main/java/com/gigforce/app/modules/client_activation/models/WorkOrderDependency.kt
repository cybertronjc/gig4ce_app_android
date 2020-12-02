package com.gigforce.app.modules.client_activation.models

import com.gigforce.app.modules.landingscreen.models.Dependency

data class WorkOrderDependency(
    var requiredFeatures: List<Dependency>? = null,
    var coverImg: String? = null,
    var subTitle: String? = null,
    var title: String? = null,
    var type: String? = null,
    var workOrderId: String? = null,
    var nextDependency: String = "",
    var jobProfileId: String = "",
    var step: Int = 2
)