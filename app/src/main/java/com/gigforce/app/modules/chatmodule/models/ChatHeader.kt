package com.gigforce.app.modules.chatmodule.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class ChatHeader(
    @DocumentId
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("forUserId")
    @set:PropertyName("forUserId")
    var forUserId: String = "",

    @get:PropertyName("otherUserId")
    @set:PropertyName("otherUserId")
    var otherUserId: String = "",

    @get:PropertyName("lastMsgText")
    @set:PropertyName("lastMsgText")
    var lastMsgText: String = "",

    @get:PropertyName("lastMessageType")
    @set:PropertyName("lastMessageType")
    var lastMessageType: String = "",

    @get:PropertyName("lastMsgTimestamp")
    @set:PropertyName("lastMsgTimestamp")
    var lastMsgTimestamp: com.google.firebase.Timestamp? = null,

    @get:PropertyName("unseenCount")
    @set:PropertyName("unseenCount")
    var unseenCount: Int = 0,

    @get:PropertyName("otherUser")
    @set:PropertyName("otherUser")
    var otherUser: UserInfo? = null
) : BaseFirestoreDataModel(tableName = "headers") {
}

data class UserInfo(
    @DocumentId
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("profilePic")
    @set:PropertyName("profilePic")
    var profilePic: String = "",

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = ""
) {}