package com.gigforce.app.modules.chatmodule.models

import java.time.LocalDate



//Base Class for Chat Messages
open class GroupChatMessage(
    private val messageType: MessageType,
    private val message : GroupMessage? = null
) {

    fun getMessageType(): MessageType = messageType

    fun toMessage(): GroupMessage {

        if (messageType == MessageType.DATE){
            throw IllegalArgumentException("date item cannot be converted into chat message")
        }

        return message!!
    }

    companion object {

        fun fromMessage(message: GroupMessage) : GroupChatMessage = when(message.type){
            Message.MESSAGE_TYPE_TEXT -> GroupTextChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_IMAGE -> GroupImageChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_VIDEO -> GroupVideoChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_AUDIO -> GroupAudioChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> GroupDocumentChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_CONTACT -> GroupContactChatMessage(message)
            else -> GroupUnsupportedChatMessage()
        }
    }
}

class GroupDateChatMessage : GroupChatMessage(
    MessageType.DATE
)

class GroupTextChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT,
    message
)

class GroupImageChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT_WITH_IMAGE,
    message
)

class GroupVideoChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT_WITH_VIDEO,
    message
)

class GroupLocationChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT_WITH_LOCATION,
    message
)

class GroupContactChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT_WITH_CONTACT,
    message
)

class GroupAudioChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT_WITH_AUDIO,
    message
)

class GroupDocumentChatMessage(message: GroupMessage) : GroupChatMessage(
    MessageType.TEXT_WITH_DOCUMENT,
    message
)

class GroupUnsupportedChatMessage : GroupChatMessage(
    MessageType.NOT_SUPPORTED
)

class GroupChatDateItem constructor(
    private val date : LocalDate
) : GroupChatMessage(
    MessageType.DATE
){

    fun createItemTypeFor(date: LocalDate): GroupChatMessage {
        return GroupChatDateItem(date)
    }

    fun createDateItemForToday(): GroupChatMessage {
        return createItemTypeFor(LocalDate.now())
    }

    fun getDate() = date
}
