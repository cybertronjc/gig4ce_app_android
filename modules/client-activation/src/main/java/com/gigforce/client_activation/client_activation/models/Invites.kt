package com.gigforce.client_activation.client_activation.models

import java.util.*

data class Invites(
    var invite_id: String = "",
    var timestamp: Date = Date(),
    var role: String = "",
    var jobProfileId: String = ""
) {
}