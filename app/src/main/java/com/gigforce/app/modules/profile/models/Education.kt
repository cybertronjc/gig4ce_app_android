package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

data class Education(
    var brief: String = "",
    var institution: String = "",
    var course: String = "",
    var degree: String = "",
    var startYear: Date? = null,
    var endYear: Date? = null,
    var validateFields: Boolean = false
) : BaseFirestoreDataModel(tableName = "educations") {
}