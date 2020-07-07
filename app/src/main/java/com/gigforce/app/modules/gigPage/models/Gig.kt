package com.gigforce.app.modules.gigPage.models

import java.util.*

data class Gig(
    var gigId: String,
    var title: String,
    var startDate : Date,
    var companyName: String?,
    var contactNo: String?,
    var gigType: String?,
    var gigDetails: GigDetails,
    var gigHighLights: List<String>,
    var gigRequirements: List<String>,
    var gigLocationDetails: GigLocationDetails?,
    var attendance : GigAttendance?,
    var gigContactDetails: GigContactDetails
)