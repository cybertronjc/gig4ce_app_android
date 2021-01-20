package com.gigforce.modules.feature_chat.models

import java.time.LocalDate

enum class MessageType {
    DATE,
    TEXT,
    TEXT_WITH_IMAGE,
    TEXT_WITH_VIDEO,
    TEXT_WITH_LOCATION,
    TEXT_WITH_CONTACT,
    TEXT_WITH_AUDIO,
    TEXT_WITH_DOCUMENT,
    NOT_SUPPORTED;
}

//Base Class for Chat Messages
open class OldChatMessage(
    private val messageType: MessageType,
    private val chatMessage : ChatMessage? = null
) {

    fun getMessageType(): MessageType = messageType

    fun toMessage(): ChatMessage {

        if (messageType == MessageType.DATE){
            throw IllegalArgumentException("date item cannot be converted into chat message")
        }

        return chatMessage!!
    }

    companion object {

        fun fromMessage(chatMessage: ChatMessage) : OldChatMessage = when(chatMessage.type){
            ChatMessage.MESSAGE_TYPE_TEXT -> TextChatMessage(chatMessage)
            ChatMessage.MESSAGE_TYPE_TEXT_WITH_IMAGE -> ImageChatMessage(chatMessage)
            ChatMessage.MESSAGE_TYPE_TEXT_WITH_VIDEO -> VideoChatMessage(chatMessage)
            ChatMessage.MESSAGE_TYPE_TEXT_WITH_AUDIO -> AudioChatMessage(chatMessage)
            ChatMessage.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> DocumentChatMessage(chatMessage)
            ChatMessage.MESSAGE_TYPE_TEXT_WITH_CONTACT -> ContactChatMessage(chatMessage)
            else -> UnsupportedChatMessage()
        }
    }
}

class DateChatMessage : OldChatMessage(
    MessageType.DATE
)

class TextChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT,
    chatMessage
)

class ImageChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT_WITH_IMAGE,
    chatMessage
)

class VideoChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT_WITH_VIDEO,
    chatMessage
)

class LocationChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT_WITH_LOCATION,
    chatMessage
)

class ContactChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT_WITH_CONTACT,
    chatMessage
)

class AudioChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT_WITH_AUDIO,
    chatMessage
)

class DocumentChatMessage(chatMessage: ChatMessage) : OldChatMessage(
    MessageType.TEXT_WITH_DOCUMENT,
    chatMessage
)

class UnsupportedChatMessage : OldChatMessage(
    MessageType.NOT_SUPPORTED
)

class ChatDateItem constructor(
    private val date : LocalDate
) : OldChatMessage(
    MessageType.DATE
){

    fun createItemTypeFor(date: LocalDate): OldChatMessage {
        return ChatDateItem(date)
    }

    fun createDateItemForToday(): OldChatMessage {
        return createItemTypeFor(LocalDate.now())
    }

    fun getDate() = date
}




