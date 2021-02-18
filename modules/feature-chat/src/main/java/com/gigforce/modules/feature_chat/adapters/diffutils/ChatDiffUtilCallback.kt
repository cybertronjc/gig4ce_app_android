package com.gigforce.modules.feature_chat.adapters.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.modules.feature_chat.models.ChatMessage

class ChatDiffUtilCallback(
    private val oldChatList: List<ChatMessage>,
    private val newChatList: List<ChatMessage>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldChatList.size
    }

    override fun getNewListSize(): Int {
        return newChatList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldChatList[oldItemPosition].toMessage().id == newChatList[newItemPosition].toMessage().id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldChatList[oldItemPosition].toMessage()
        val newMessage = newChatList[newItemPosition].toMessage()

        return oldMessage.id == newMessage.id &&
                oldMessage.attachmentPath == newMessage.attachmentPath &&
                oldMessage.thumbnail == newMessage.thumbnail &&
                (oldMessage.thumbnailBitmap == null && newMessage.thumbnailBitmap != null)
    }
}