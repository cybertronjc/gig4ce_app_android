package com.gigforce.app.modules.client_activation.models

import com.gigforce.app.modules.landingscreen.models.Dependency

data class WorkOrderDependency(
        var dependency: ArrayList<Dependency>? = null,
        var icon: String? = null,
        var sub_title: String? = null,
        var title: String? = null,
        var type: String? = null,
        var work_order_id: String? = null
)