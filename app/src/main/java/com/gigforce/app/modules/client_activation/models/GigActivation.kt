package com.gigforce.app.modules.client_activation.models

import com.gigforce.app.modules.landingscreen.models.Dependency

data class GigActivation(
    var coverImg: String = "",
    var subTitle: String = "",
    var title: String = "",
    var type: String = "",
    var jobProfileId: String = "",
    var step: Int = 0,
    var requiredFeatures: List<Dependency> = listOf(),
    var instruction: String = ""
)