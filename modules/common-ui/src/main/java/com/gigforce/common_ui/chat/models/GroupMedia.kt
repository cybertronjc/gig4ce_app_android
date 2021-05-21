package com.gigforce.common_ui.chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

class GroupMedia(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("groupHeaderId")
    @set:PropertyName("groupHeaderId")
    var groupHeaderId: String = "",

    @get:PropertyName("messageId")
    @set:PropertyName("messageId")
    var messageId: String = "",

    @get:PropertyName("attachmentType")
    @set:PropertyName("attachmentType")
    var attachmentType: String = "",

    @get:PropertyName("videoAttachmentLength")
    @set:PropertyName("videoAttachmentLength")
    var videoAttachmentLength: Long = 0,

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Timestamp? = null,

    @get:PropertyName("thumbnail")
    @set:PropertyName("thumbnail")
    var thumbnail: String? = null,

    @get:PropertyName("attachmentName")
    @set:PropertyName("attachmentName")
    var attachmentName: String? = null,

    @get:PropertyName("attachmentPath")
    @set:PropertyName("attachmentPath")
    var attachmentPath: String? = null,

    @get:PropertyName("senderInfo")
    @set:PropertyName("senderInfo")
    var senderInfo: UserInfo? = null
)