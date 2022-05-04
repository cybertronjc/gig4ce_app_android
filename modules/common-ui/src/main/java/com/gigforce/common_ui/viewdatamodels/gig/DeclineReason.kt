package com.gigforce.common_ui.viewdatamodels.gig

import com.google.firebase.firestore.PropertyName

data class DeclineReason(

    @get:PropertyName("reason_id")
    @set:PropertyName("reason_id")
    var reasonId: String = "",

    @get:PropertyName("reason")
    @set:PropertyName("reason")
    var reason: String = "",
)