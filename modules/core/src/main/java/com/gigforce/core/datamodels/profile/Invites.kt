package com.gigforce.core.datamodels.profile

import java.util.*

data class Invites(
    var invite_id: String = "",
    var timestamp: Date = Date(),
    var role: String = "",
    var jobProfileId: String = ""
) {
}