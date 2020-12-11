package com.gigforce.app.modules.client_activation.models

import com.gigforce.app.modules.landingscreen.models.Dependency
import java.util.*

data class JpApplication(
        var id: String = "", var JPId: String = "", var approvedBy: String = "", var approvedOn: String = "", var gigerId: String = "",
        var rejectedBy: String = "", var rejectedOn: String = "", var status: String = "Draft", var stepDone: Int = 1, var stepsTotal: Int = 0,
        var submitOn: String = "", var draft: MutableList<Dependency> = mutableListOf(), var process: MutableList<Dependency> = mutableListOf(), var applyOn: Date = Date()


)