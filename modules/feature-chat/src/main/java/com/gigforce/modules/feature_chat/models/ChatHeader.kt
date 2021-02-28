package com.gigforce.modules.feature_chat.models

import com.gigforce.core.fb.BaseFirestoreDataModel
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class ChatHeader(
    @DocumentId
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("isBlocked")
    @set:PropertyName("isBlocked")
    var isBlocked: Boolean = false,

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
    var otherUser: UserInfo? = null,

    @get:PropertyName("chatType")
    @set:PropertyName("chatType")
    var chatType: String = "",

    @get:PropertyName("groupId")
    @set:PropertyName("groupId")
    var groupId: String = "",

    @get:PropertyName("groupName")
    @set:PropertyName("groupName")
    var groupName: String = "",

    @get:PropertyName("groupAvatar")
    @set:PropertyName("groupAvatar")
    var groupAvatar: String = "",

    @get:PropertyName("removedFromGroup")
    @set:PropertyName("removedFromGroup")
    var removedFromGroup: Boolean = false,

    @get:PropertyName("groupDeactivated")
    @set:PropertyName("groupDeactivated")
    var groupDeactivated: Boolean = false

) : BaseFirestoreDataModel(tableName = "headers")