package com.gigforce.modules.feature_chat.models

import androidx.lifecycle.LifecycleOwner
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.ViewTypes
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.core.SimpleDVM
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.firebase.auth.FirebaseAuth

data class ChatMessageWrapper(
        val message: ChatMessage,
        val oneToOneChatViewModel: ChatPageViewModel,
        val groupChatViewModel: GroupChatViewModel,
        val lifeCycleOwner: LifecycleOwner
) : SimpleDVM(
        defaultViewType = -1,
        onClickNavPath = null
) {

    override fun getViewType(): Int {

        if (this.message.isDeleted) {
            if (this.message.chatType == ChatConstants.CHAT_TYPE_USER) {
                return if (this.message.flowType == "in") ViewTypes.IN_DELETED_MESSAGE else ViewTypes.OUT_DELETED_MESSAGE
            } else if (this.message.chatType == ChatConstants.CHAT_TYPE_GROUP) {
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
                return if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_DELETED_MESSAGE else ViewTypes.GROUP_OUT_DELETED_MESSAGE
            }
        } else if (this.message.isMessageChatEvent) {
            return ViewTypes.CHAT_EVENT
        } else if (this.message.chatType == ChatConstants.CHAT_TYPE_USER) {

            return when (this.message.type) {
                ChatConstants.MESSAGE_TYPE_TEXT -> if (this.message.flowType == "in") ViewTypes.IN_TEXT else ViewTypes.OUT_TEXT
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> if (this.message.flowType == "in") ViewTypes.IN_IMAGE else ViewTypes.OUT_IMAGE
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> if (this.message.flowType == "in") ViewTypes.IN_DOCUMENT else ViewTypes.OUT_DOCUMENT
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> if (this.message.flowType == "in") ViewTypes.IN_VIDEO else ViewTypes.OUT_VIDEO
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> if (this.message.flowType == "in") ViewTypes.IN_LOCATION else ViewTypes.OUT_LOCATION
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> if(this.message.flowType == "in") ViewTypes.IN_AUDIO else ViewTypes.OUT_AUDIO
                else -> -1
            }
        } else if (this.message.chatType == ChatConstants.CHAT_TYPE_GROUP) {

            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
            return when (this.message.type) {
                ChatConstants.MESSAGE_TYPE_TEXT -> if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_TEXT else ViewTypes.GROUP_OUT_TEXT
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_IMAGE else ViewTypes.GROUP_OUT_IMAGE
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_DOCUMENT else ViewTypes.GROUP_OUT_DOCUMENT
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_VIDEO else ViewTypes.GROUP_OUT_VIDEO
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_LOCATION else ViewTypes.GROUP_OUT_LOCATION
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> if (this.message.senderInfo.id != currentUserId) ViewTypes.GROUP_IN_AUDIO else ViewTypes.GROUP_OUT_AUDIO
                else -> -1
            }
        }

        return -1
    }
}
