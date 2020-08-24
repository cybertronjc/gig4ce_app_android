package com.gigforce.app.modules.chatmodule.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.DocumentId

data class ChatHeader(
    @DocumentId
    var id: String = "",
    var forUserId: String = "",
    var lastMsgText: String = "",
    var lastMsgTimestamp: com.google.firebase.Timestamp? = null,
    var unseenCount: Int = 0,
    var otherUser: UserInfo? = null
): BaseFirestoreDataModel(tableName = "chat_headers") {
}

data class UserInfo(
   var name: String = "",
   var profilePic: String = "",
   var type: String = "",
   @DocumentId
   var id: String = ""
) {}