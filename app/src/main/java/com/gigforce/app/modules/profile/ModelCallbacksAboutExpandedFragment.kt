package com.gigforce.app.modules.profile

import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactPhone

interface ModelCallbacksAboutExpandedFragment {
    fun setIsWhatsAppNumber(
        profileID: String,
        contactList: ArrayList<ContactPhone>,
        number: String,
        isWhatsAppNumber: Boolean,
        responseCallbacks: ResponseModelCallbacksAboutExpandedFragment
    )

    fun updateContactAndEmailSeparately(
        profileID: String,
        contactList: ArrayList<Contact>,
        responseCallbacks: ResponseModelCallbacksAboutExpandedFragment
    )


    interface ResponseModelCallbacksAboutExpandedFragment {
        fun reloadProfile(loadOffline: Boolean)
    }

}