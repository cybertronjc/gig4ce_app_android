package com.gigforce.app.modules.client_activation.models


data class GigActivation(
    var coverImg: String = "",
    var subTitle: String = "",
    var title: String = "",
    var type: String = "",
    var jobProfileId: String = "",
    var step: Int = 0,
    var requiredFeatures: List<Dependency> = listOf(),
    var instruction: String = "",
    var videoUrl: String = ""
)