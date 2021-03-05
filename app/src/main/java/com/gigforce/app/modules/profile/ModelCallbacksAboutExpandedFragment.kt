package com.gigforce.app.modules.profile

import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.datamodels.profile.ContactEmail
import com.gigforce.core.datamodels.profile.ContactPhone

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

    fun updateContact(
        profileID: String,
        contactList: ArrayList<ContactPhone>?,
        newContact: ContactPhone?,
        oldContact: String?,
        add: Boolean?,
        delete: Boolean?,
        responseCallbacks: ResponseModelCallbacksAboutExpandedFragment
    )

    fun updateEmail(
        profileID: String,
        contactList: ArrayList<ContactEmail>?,
        newContact: ContactEmail?,
        oldContact: String?,
        add: Boolean?,
        delete: Boolean?,
        responseCallbacks: ResponseModelCallbacksAboutExpandedFragment
    )


    interface ResponseModelCallbacksAboutExpandedFragment {
        fun reloadProfile(loadOffline: Boolean)
        fun errorUpdatingContact(error: String)
    }

}