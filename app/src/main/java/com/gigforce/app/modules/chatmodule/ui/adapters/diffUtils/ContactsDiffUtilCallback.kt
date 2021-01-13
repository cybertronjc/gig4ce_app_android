package com.gigforce.app.modules.chatmodule.ui.adapters.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.app.modules.chatmodule.models.ContactModel

class ContactsDiffUtilCallback(
    private val oldContactList: List<ContactModel>,
    private val newContactList: List<ContactModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldContactList.size
    }

    override fun getNewListSize(): Int {
        return newContactList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldContactList[oldItemPosition].id.equals(newContactList[newItemPosition].id)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldContact = oldContactList[oldItemPosition]
        val newContact = newContactList[newItemPosition]
        return oldContact.id == newContact.id &&
                oldContact.isGigForceUser == newContact.isGigForceUser &&
                oldContact.imageUrl == newContact.imageUrl &&
                oldContact.name == newContact.name
    }
}