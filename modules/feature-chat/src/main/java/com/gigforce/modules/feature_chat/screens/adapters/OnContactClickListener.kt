package com.gigforce.modules.feature_chat.screens.adapters

import com.gigforce.modules.feature_chat.models.ContactModel

interface OnContactClickListener {

    fun contactClick(contact: ContactModel)

    fun onContactSelected(selectedContactsCount : Int)
}