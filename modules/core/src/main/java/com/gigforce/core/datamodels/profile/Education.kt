package com.gigforce.core.datamodels.profile

import com.gigforce.core.fb.BaseFirestoreDataModel
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