package com.gigforce.modules.feature_chat.adapters.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.modules.feature_chat.models.OldChatMessage

class ChatDiffUtilCallback(
        private val oldOldChatList: List<OldChatMessage>,
        private val newOldChatList: List<OldChatMessage>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldOldChatList.size
    }

    override fun getNewListSize(): Int {
        return newOldChatList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldOldChatList[oldItemPosition].toMessage().id == newOldChatList[newItemPosition].toMessage().id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldOldChatList[oldItemPosition].toMessage()
        val newMessage = newOldChatList[newItemPosition].toMessage()

        return oldMessage.id == newMessage.id &&
                oldMessage.attachmentPath == newMessage.attachmentPath &&
                oldMessage.thumbnail == newMessage.thumbnail &&
                (oldMessage.thumbnailBitmap == null && newMessage.thumbnailBitmap != null)
    }
}