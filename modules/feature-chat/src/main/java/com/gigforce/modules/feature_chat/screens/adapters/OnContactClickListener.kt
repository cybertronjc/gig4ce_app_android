package com.gigforce.modules.feature_chat.screens.adapters

import com.gigforce.common_ui.chat.models.ContactModel

interface OnContactClickListener {

    fun contactClick(contact: ContactModel)

    fun onContactSelected(selectedContactsCount : Int)
}