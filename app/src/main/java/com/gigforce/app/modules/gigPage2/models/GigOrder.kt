package com.gigforce.app.modules.gigPage2.models

import com.gigforce.app.modules.ambassador_user_enrollment.models.City
import com.gigforce.app.modules.ambassador_user_enrollment.models.State
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class GigOrder(

        @get:PropertyName("endDate")
        @set:PropertyName("endDate")
        var endDate: Timestamp = Timestamp.now(),

        @get:PropertyName("city")
        @set:PropertyName("city")
        var city: City? = null,

        @get:PropertyName("state")
        @set:PropertyName("state")
        var state: State? = null,

        @get:PropertyName("locationType")
        @set:PropertyName("locationType")
        var locationType: String = "",

        @get:PropertyName("office")
        @set:PropertyName("office")
        var workOrderOffice: WorkOrderOffice? = null
) {
    @Exclude
    fun getGigOrderCity(): String? {
        return workOrderOffice?.city?.name ?: city?.name
    }

    @Exclude
    fun getGigOrderState(): String? {
        return workOrderOffice?.state?.name ?: state?.name
    }
}

data class WorkOrderOffice(

        @get:PropertyName("city")
        @set:PropertyName("city")
        var city: City? = null,

        @get:PropertyName("state")
        @set:PropertyName("state")
        var state: State? = null

)
