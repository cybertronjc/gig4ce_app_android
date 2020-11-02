package com.gigforce.app.modules.chatmodule.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.DocumentId

data class ChatHeader(
    @DocumentId
    val id: String = "",
    val forUserId: String = "",
    val otherUserId: String = "",
    val lastMsgText: String = "",
    val lastMsgTimestamp: com.google.firebase.Timestamp? = null,
    val unseenCount: Int = 0,
    val otherUser: UserInfo? = null
): BaseFirestoreDataModel(tableName = "headers") {
}

data class UserInfo(
    @DocumentId
    var id: String = "",
    var name: String = "",
   var profilePic: String = "",
   var type: String = ""
) {}