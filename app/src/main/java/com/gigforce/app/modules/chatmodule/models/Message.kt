package com.gigforce.app.modules.chatmodule.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

class Message(
    @DocumentId
    var id: String = "",
    var headerId: String = "",
    var forUserId: String = "",
    var otherUserId: String = "",
    var flowType: String = "",
    var timestamp: Timestamp? = null,
    var status: Int = 0,
    var type: String = "",
    var content: String = ""
) {
}