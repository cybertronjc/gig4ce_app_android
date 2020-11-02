package com.gigforce.app.modules.chatmodule.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue

class Message(
    @DocumentId
    val id: String = "",
    val headerId: String = "",
    val forUserId: String = "",
    val otherUserId: String = "",
    val flowType: String = "",
    val timestamp: Timestamp? = null,
    var status: Int = 0,
    val type: String = "",
    val content: String = ""
) {
}