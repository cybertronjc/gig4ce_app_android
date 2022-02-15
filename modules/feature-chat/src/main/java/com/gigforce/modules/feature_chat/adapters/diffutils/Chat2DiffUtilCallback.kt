package com.gigforce.modules.feature_chat.adapters.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.core.recyclerView.CoreDiffUtilCallback
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper

class Chat2DiffUtilCallback() : CoreDiffUtilCallback<ChatMessageWrapper>() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].message.id == newList[newItemPosition].message.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldList[oldItemPosition]
        val newMessage = newList[newItemPosition]

        return oldMessage.message.id == newMessage.message.id &&
                oldMessage.message.attachmentPath == newMessage.message.attachmentPath &&
                oldMessage.message.thumbnail == newMessage.message.thumbnail &&
                (oldMessage.message.thumbnailBitmap == null && newMessage.message.thumbnailBitmap != null) &&
                oldMessage.message.status == oldMessage.message.status &&
                oldMessage.message.isDeleted == oldMessage.message.isDeleted &&
                oldMessage.message.isMessageChatEvent == newMessage.message.isMessageChatEvent &&
                oldMessage.message.location == newMessage.message.location &&
                oldMessage.message.deletedOn == newMessage.message.deletedOn &&
                oldMessage.message.locationPhysicalAddress == newMessage.message.locationPhysicalAddress &&
                oldMessage.message.isCurrentlySharingLiveLocation == newMessage.message.isCurrentlySharingLiveLocation
    }
}