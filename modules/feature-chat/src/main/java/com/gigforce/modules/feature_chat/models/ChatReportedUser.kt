package com.gigforce.modules.feature_chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ChatReportedUser(

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String? = null,

    @get:PropertyName("reportedUserUid")
    @set:PropertyName("reportedUserUid")
    var reportedUserUid: String = "",

    @get:PropertyName("reportedBy")
    @set:PropertyName("reportedBy")
    var reportedBy: String? = null,

    @get:PropertyName("reportedOn")
    @set:PropertyName("reportedOn")
    var reportedOn: Timestamp = Timestamp.now(),

    @get:PropertyName("reportingReason")
    @set:PropertyName("reportingReason")
    var reportingReason: String = ""
)