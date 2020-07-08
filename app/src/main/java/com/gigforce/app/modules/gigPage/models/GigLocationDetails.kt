package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import java.util.*

@Keep
data class GigLocationDetails(
    var latitude: Double?,
    var longitude: Double?,
    var fullAddress: String?
)