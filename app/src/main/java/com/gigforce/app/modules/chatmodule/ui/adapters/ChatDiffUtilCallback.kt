package com.gigforce.app.modules.chatmodule.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.app.modules.chatmodule.models.ChatMessage

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
        return newChatList[oldItemPosition].getMessageType().equals(newChatList[newItemPosition].getMessageType())
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldChatList[oldItemPosition]
        val newMessage = newChatList[newItemPosition]
        return true
    }
}