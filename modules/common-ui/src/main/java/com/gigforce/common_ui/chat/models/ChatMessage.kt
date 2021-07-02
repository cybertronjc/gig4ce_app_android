package com.gigforce.common_ui.chat.models

import android.graphics.Bitmap
import com.gigforce.common_ui.viewdatamodels.chat.UserInfo
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

    /**
     * One to one chat or group Chat
     */
    @get:PropertyName("chatType")
    @set:PropertyName("chatType")
    var chatType: String = "",

    @get:PropertyName("content")
    @set:PropertyName("content")
    var content: String = "",

    @get:PropertyName("mentionedUsersInfo")
    @set:PropertyName("mentionedUsersInfo")
    var mentionedUsersInfo: List<MentionUser> = emptyList(),

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
    var receiverInfo: UserInfo? = UserInfo(),

    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    @get:PropertyName("deletedOn")
    @set:PropertyName("deletedOn")
    var deletedOn: Timestamp? = null,

    @get:PropertyName("groupMessageReadBy")
    @set:PropertyName("groupMessageReadBy")
    var groupMessageReadBy: List<MessageReceivingInfo> = emptyList(),

    @get:PropertyName("groupId")
    @set:PropertyName("groupId")
    var groupId: String = "",

    @get:Exclude
    @set:Exclude
    var thumbnailBitmap: Bitmap? = null

) : IMediaMessage

interface IMediaMessage {
    var type: String
    var attachmentName: String?
    var attachmentPath: String?
}