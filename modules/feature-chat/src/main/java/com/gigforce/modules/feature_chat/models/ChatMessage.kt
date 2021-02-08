package com.gigforce.modules.feature_chat.models

import android.graphics.Bitmap
import com.gigforce.core.DataViewObject
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.core.ViewTypes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

class ChatMessage(
        @DocumentId
        @get:PropertyName("id")
        @set:PropertyName("id")
        var id: String = "",

        @get:PropertyName("senderMessageId")
        @set:PropertyName("senderMessageId")
        var senderMessageId: String = "",

        @get:PropertyName("headerId")
        @set:PropertyName("headerId")
        var headerId: String = "",

        @get:PropertyName("flowType")
        @set:PropertyName("flowType")
        var flowType: String = "",

        @get:PropertyName("timestamp")
        @set:PropertyName("timestamp")
        var timestamp: Timestamp? = null,

        @get:PropertyName("status")
        @set:PropertyName("status")
        var status: Int = 0,

        @get:PropertyName("type")
        @set:PropertyName("type")
        override var type: String = "",

        @get:PropertyName("content")
        @set:PropertyName("content")
        var content: String = "",

        @get:PropertyName("videoLength")
        @set:PropertyName("videoLength")
        var videoLength: Long = 0,

        /**
         * Attachment Path- full path of image, video etc
         */
        @get:PropertyName("thumbnail")
        @set:PropertyName("thumbnail")
        var thumbnail: String? = null,


        @get:PropertyName("attachmentName")
        @set:PropertyName("attachmentName")
        override var attachmentName: String? = null,

        /**
         * Attachment Path- full path of image, video etc
         */
        @get:PropertyName("attachmentPath")
        @set:PropertyName("attachmentPath")
        override var attachmentPath: String? = null,

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

        @get:PropertyName("locationPhysicalAddress")
        @set:PropertyName("locationPhysicalAddress")
        var locationPhysicalAddress: String = "",

        @get:PropertyName("location")
        @set:PropertyName("location")
        var location: GeoPoint? = null,

        @get:PropertyName("senderInfo")
        @set:PropertyName("senderInfo")
        var senderInfo: UserInfo = UserInfo(),

        @get:PropertyName("receiverInfo")
        @set:PropertyName("receiverInfo")
        var receiverInfo: UserInfo = UserInfo(),

        @get:Exclude
        @set:Exclude
        var thumbnailBitmap: Bitmap? = null,

        @get:Exclude
        @set:Exclude
        var attachmentCurrentlyBeingDownloaded: Boolean = false
) : DataViewObject(),
        IMediaMessage {

    override fun getViewType(): Int {

        return when (this.type) {
            ChatConstants.MESSAGE_TYPE_TEXT -> if (this.flowType == "in") ViewTypes.IN_TEXT else ViewTypes.OUT_TEXT
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> if (this.flowType == "in") ViewTypes.IN_IMAGE else ViewTypes.OUT_IMAGE
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> if (this.flowType == "in") ViewTypes.IN_DOCUMENT else ViewTypes.OUT_DOCUMENT
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> if (this.flowType == "in") ViewTypes.IN_VIDEO else ViewTypes.OUT_VIDEO
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> if (this.flowType == "in") ViewTypes.IN_LOCATION else ViewTypes.OUT_LOCATION
            else -> -1
        }
    }
}

interface IMediaMessage {
    var type: String
    var attachmentName: String?
    var attachmentPath: String?
}