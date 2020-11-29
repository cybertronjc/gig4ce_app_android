package com.gigforce.app.modules.client_activation.models

import java.util.*

data class JpApplication(var JPId: String = "", var approvedBy: String = "", var approvedOn: String = "", var gigerId: String = "",
                         var rejectedBy: String = "", var rejectedOn: String = "", var status: String = "draft", var stepDone: Int = 1, var stepsTotal: Int = 0, var
                         submitOn: String = "", var draft: List<JpDraft> = listOf(), var process: List<JpProcess> =listOf(), var applyOn: Date = Date())