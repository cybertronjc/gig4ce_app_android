package com.gigforce.profile.datamodel

import java.util.*

data class Invites(
    var invite_id: String = "",
    var timestamp: Date = Date(),
    var role: String = "",
    var jobProfileId: String = ""
) {
}

