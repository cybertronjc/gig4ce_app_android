package com.gigforce.app.modules.gigPage.models

import java.util.*

data class GigDetails(
    var startTime: Date,
    var endTime: Date,
    var wage: String?,
    var shiftDuration : String?,
    var address: String?
)