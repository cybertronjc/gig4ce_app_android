package com.gigforce.app.modules.chatmodule

import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.profile.models.Contact

interface OnContactsSelectedListener {

   fun onContactsSelected(contacts : List<ContactModel>)
}