package com.gigforce.app.modules.profile.models

import java.util.*

data class Invites(
    var invite_id: String = "",
    var timestamp: Date = Date(),
    var role: String = "",
    var jobProfileId: String = ""
) {
}