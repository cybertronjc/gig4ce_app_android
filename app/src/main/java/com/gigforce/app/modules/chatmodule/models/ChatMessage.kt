package com.gigforce.app.modules.chatmodule.models

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
open class ChatMessage(
    private val messageType: MessageType,
    private val message : Message? = null
) {

    fun getMessageType(): MessageType = messageType

    fun toMessage(): Message {

        if (messageType == MessageType.DATE){
            throw IllegalArgumentException("date item cannot be converted into chat message")
        }

        return message!!
    }

    companion object {

        fun fromMessage(message: Message) : ChatMessage = when(message.type){
            Message.MESSAGE_TYPE_TEXT -> TextChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_IMAGE -> ImageChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_VIDEO -> VideoChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_AUDIO -> AudioChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> DocumentChatMessage(message)
            Message.MESSAGE_TYPE_TEXT_WITH_CONTACT -> ContactChatMessage(message)
            else -> UnsupportedChatMessage()
        }
    }
}

class DateChatMessage : ChatMessage(
    MessageType.DATE
)

class TextChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT,
    message
)

class ImageChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT_WITH_IMAGE,
    message
)

class VideoChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT_WITH_VIDEO,
    message
)

class LocationChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT_WITH_LOCATION,
    message
)

class ContactChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT_WITH_CONTACT,
    message
)

class AudioChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT_WITH_AUDIO,
    message
)

class DocumentChatMessage(message: Message) : ChatMessage(
    MessageType.TEXT_WITH_DOCUMENT,
    message
)

class UnsupportedChatMessage : ChatMessage(
    MessageType.NOT_SUPPORTED
)

class ChatDateItem constructor(
    private val date : LocalDate
) : ChatMessage(
    MessageType.DATE
){

    fun createItemTypeFor(date: LocalDate): ChatMessage {
        return ChatDateItem(date)
    }

    fun createDateItemForToday(): ChatMessage {
        return createItemTypeFor(LocalDate.now())
    }

    fun getDate() = date
}




