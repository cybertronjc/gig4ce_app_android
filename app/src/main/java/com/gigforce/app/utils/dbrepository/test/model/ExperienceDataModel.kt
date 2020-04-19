package com.gigforce.app.utils.dbrepository.test.model

import com.gigforce.app.utils.dbrepository.BaseDataModel
import java.util.*

class ExperienceDataModel:BaseDataModel{
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