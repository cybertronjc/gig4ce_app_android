package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import java.util.*

@Keep
data class GigLocationDetails(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var fullAddress: String? = ""
)