package com.gigforce.app.modules.chatmodule

import com.gigforce.app.modules.chatmodule.models.ContactModel

interface OnContactsSelectedListener {

   fun onContactsSelected(contacts : List<ContactModel>)
}