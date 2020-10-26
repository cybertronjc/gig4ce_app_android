package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude
import java.util.*

data class Experience(
    var title: String = "",
    var employmentType: String = "",
    var company: String = "",
    var location: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null,
    @get:Exclude var validateFields: Boolean = false,
    var isFresher: Boolean = false,
    var currentExperience: Boolean = false
) : BaseFirestoreDataModel(tableName = "experiences") {
}