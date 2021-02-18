package com.gigforce.modules.feature_chat.models

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

class GroupMessage(
    @DocumentId
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("groupHeaderId")
    @set:PropertyName("groupHeaderId")
    var groupHeaderId: String = "",

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Timestamp? = null,

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: Int = 0,

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "",

    @get:PropertyName("content")
    @set:PropertyName("content")
    var content: String = "",

    @get:PropertyName("senderInfo")
    @set:PropertyName("senderInfo")
    var senderInfo: UserInfo? = null,

    /**
     * Attachment Path- full path of image, video etc
     */
    @get:PropertyName("thumbnail")
    @set:PropertyName("thumbnail")
    var thumbnail: String? = null,

    @get:PropertyName("attachmentName")
    @set:PropertyName("attachmentName")
    var attachmentName: String? = null,

    @get:PropertyName("videoAttachmentLength")
    @set:PropertyName("videoAttachmentLength")
    var videoAttachmentLength: Long = 0,

    /**
     * Attachment Path- full path of image, video etc
     */
    @get:PropertyName("attachmentPath")
    @set:PropertyName("attachmentPath")
    var attachmentPath: String? = null,

    /**
     * Contact details Specific Fields
     */
    @get:PropertyName("contactName")
    @set:PropertyName("contactName")
    var contactName: String = "",

    @get:PropertyName("contactNumber")
    @set:PropertyName("contactNumber")
    var contactNumber: String = "",


    /**
     * Location message payload
     */

    @get:PropertyName("latitude")
    @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude")
    @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:Exclude
    @set:Exclude
    var thumbnailBitmap: Bitmap? = null
)