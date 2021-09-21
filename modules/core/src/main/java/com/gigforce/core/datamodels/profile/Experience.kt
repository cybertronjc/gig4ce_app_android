package com.gigforce.core.datamodels.profile

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude
import java.util.*

data class Experience(
    var haveExperience: Boolean = false,
    var title: String = "",
    var employmentType: String = "",
    var company: String = "",
    var location: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null,
    @get:Exclude var validateFields: Boolean = false,
    var isFresher: Boolean = false,
    var currentExperience: Boolean = false,
    var role: String = "",
    var driverQuestionOwnVehicle: String = "",

    var driverQuestionVehiclesOwn: List<String> = emptyList(),

    var deliveryExecQuestionOwnVehicle: String = "",
    var deliveryQuestionVehiclesOwn: List<String> = emptyList(),
    var experienceInMonths : Int = 0
) : BaseFirestoreDataModel(tableName = "experiences") {
}