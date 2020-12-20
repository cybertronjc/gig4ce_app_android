package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude
import java.util.*

data class Experience(
    var haveExperience : Boolean = false,
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
    var earningPerMonth: Double = 0.0,
    var totalExperence : String = "",
    var driverQuestionOwnVehicle: String = "",
    var deliveryExecQuestionOwnVehicle: String = "",
    var helperComfortableLiftingHeavyWeights: Boolean = false
) : BaseFirestoreDataModel(tableName = "experiences") {
}