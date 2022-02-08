package com.gigforce.modules.feature_chat.adapters.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.common_ui.chat.models.ChatMessage

class Chat2DiffUtilCallback(
    private val oldOldChatList: List<ChatMessage>,
    private val newOldChatList: List<ChatMessage>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldOldChatList.size
    }

    override fun getNewListSize(): Int {
        return newOldChatList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldOldChatList[oldItemPosition].id == newOldChatList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldOldChatList[oldItemPosition]
        val newMessage = newOldChatList[newItemPosition]

        return oldMessage.id == newMessage.id &&
                oldMessage.attachmentPath == newMessage.attachmentPath &&
                oldMessage.thumbnail == newMessage.thumbnail &&
                (oldMessage.thumbnailBitmap == null && newMessage.thumbnailBitmap != null)
    }
}