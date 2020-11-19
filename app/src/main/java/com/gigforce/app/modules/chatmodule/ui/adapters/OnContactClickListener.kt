package com.gigforce.app.modules.chatmodule.ui.adapters

import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.ContactModel

interface OnContactClickListener {

    fun contactClick(contact: ContactModel)
}