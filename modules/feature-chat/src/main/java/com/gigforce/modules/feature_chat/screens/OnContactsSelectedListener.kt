package com.gigforce.modules.feature_chat.screens

import com.gigforce.modules.feature_chat.models.ContactModel


interface OnContactsSelectedListener {

   fun onContactsSelected(contacts : List<ContactModel>)
}