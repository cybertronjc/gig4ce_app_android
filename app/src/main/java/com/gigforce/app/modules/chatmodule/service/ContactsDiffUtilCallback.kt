package com.gigforce.app.modules.chatmodule.service

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.app.modules.chatmodule.models.ContactModel

class ContactsDiffUtilCallback(
    private val oldChatList: List<ContactModel>,
    private val newChatList: List<ContactModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldChatList.size
    }

    override fun getNewListSize(): Int {
        return newChatList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newChatList[oldItemPosition].equals(newChatList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldChatList[oldItemPosition]
        val newMessage = newChatList[newItemPosition]
        return true
    }
}