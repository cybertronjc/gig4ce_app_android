package com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners

import com.gigforce.app.modules.chatmodule.models.ContactModel

interface OnContactClickListener {

    fun contactClick(contact: ContactModel)

    fun onContactSelected(selectedContactsCount: Int)
}