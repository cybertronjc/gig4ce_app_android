package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep

@Keep
data class GigLocationDetails(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var locationPictures: List<String> = emptyList()
)