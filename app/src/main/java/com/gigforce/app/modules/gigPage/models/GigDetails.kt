package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import java.util.*

@Keep
data class GigDetails(
    var startTime: Date,
    var endTime: Date,
    var wage: String?,
    var shiftDuration : String?,
    var address: String?
)