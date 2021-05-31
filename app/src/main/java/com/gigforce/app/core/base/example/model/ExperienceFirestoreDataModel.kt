package com.gigforce.app.core.base.example.model

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

class ExperienceFirestoreDataModel:
    BaseFirestoreDataModel {
    constructor(
        title: String = "",
        employmentType: String = "",
        company: String = "",
        location: String = "",
        startDate: Date? = null,
        endDate: Date? = null,
        currentExperience: Boolean = false
    ):super("Experience")
}