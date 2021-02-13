package com.gigforce.client_activation.client_activation.models

import com.gigforce.core.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude
import java.util.*

data class Education(
    var brief: String = "",
    var institution: String = "",
    var course: String = "",
    var degree: String = "",
    var startYear: Date? = null,
    var endYear: Date? = null,
    @get:Exclude var validateFields: Boolean = false,
    var field: String = "",
    var activities: String = "",
    var educationDocument: String? = null

) : BaseFirestoreDataModel(tableName = "educations") {
}