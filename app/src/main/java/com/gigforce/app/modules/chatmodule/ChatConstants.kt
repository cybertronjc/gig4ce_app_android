package com.gigforce.app.modules.chatmodule

object ChatConstants {
    const val CHAT_TYPE_USER = "user"
    const val CHAT_TYPE_GROUP = "group"

    const val MESSAGE_TYPE_TEXT = "text"
    const val MESSAGE_TYPE_TEXT_WITH_DOCUMENT = "text_document"
    const val MESSAGE_TYPE_TEXT_WITH_IMAGE = "text_image"
    const val MESSAGE_TYPE_TEXT_WITH_VIDEO = "text_video"
    const val MESSAGE_TYPE_TEXT_WITH_AUDIO = "text_audio"
    const val MESSAGE_TYPE_TEXT_WITH_CONTACT = "text_contact"

    const val ATTACHMENT_TYPE_DOCUMENT = "document"
    const val ATTACHMENT_TYPE_IMAGE = "image"
    const val ATTACHMENT_TYPE_VIDEO = "video"
    const val ATTACHMENT_TYPE_AUDIO = "audio"

    const val DIRECTORY_APP_DATA_ROOT = "Gigforce"
    const val DIRECTORY_IMAGES = "Images"
    const val DIRECTORY_VIDEOS = "Videos"
    const val DIRECTORY_DOCUMENTS = "Documents"

    const val OPERATION_PICK_IMAGE = 0
    const val OPERATION_PICK_VIDEO = 1
    const val OPERATION_PICK_DOCUMENT = 2

    const val MB_10 = 10000000
    const val MB_15 = 15000000
    const val MB_25 = 25000000
    const val TWO_MINUTES = 120000
    const val FIVE_MINUTES = 300000
}