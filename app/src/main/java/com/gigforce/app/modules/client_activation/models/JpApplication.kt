package com.gigforce.app.modules.client_activation.models

import com.gigforce.app.modules.landingscreen.models.Dependency
import com.google.gson.annotations.SerializedName
import java.util.*

data class JpApplication(
    var id: String = "",
    var JPId: String = "",
    var approvedBy: String = "",
    var approvedOn: String = "",
    var gigerId: String = "",
    var rejectedBy: String = "",
    var rejectedOn: String = "",
    var status: String = "Draft",
    var submitOn: String = "",
    @SerializedName("application") var application: MutableList<Dependency> = mutableListOf(),
    @SerializedName("activation") var activation: MutableList<Dependency> = mutableListOf(),
    var applyOn: Date = Date()


)