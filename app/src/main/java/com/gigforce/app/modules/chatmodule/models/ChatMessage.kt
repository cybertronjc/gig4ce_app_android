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
    private val messageType: MessageType
) {

    fun getMessageType(): MessageType = messageType

    fun toMessage(): Message {

        if (messageType == MessageType.DATE){
            throw IllegalArgumentException("date item cannot be converted into chat message")
        }

        TODO()
    }

}

class DateChatMessage : ChatMessage(
    MessageType.DATE
)

class TextChatMessage : ChatMessage(
    MessageType.TEXT
)

class ImageChatMessage : ChatMessage(
    MessageType.TEXT_WITH_IMAGE
)

class VideoChatMessage : ChatMessage(
    MessageType.TEXT_WITH_VIDEO
)

class LocationChatMessage : ChatMessage(
    MessageType.TEXT_WITH_LOCATION
)

class ContactChatMessage : ChatMessage(
    MessageType.TEXT_WITH_CONTACT
)

class AudioChatMessage : ChatMessage(
    MessageType.TEXT_WITH_AUDIO
)

class DocumentChatMessage : ChatMessage(
    MessageType.TEXT_WITH_DOCUMENT
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




