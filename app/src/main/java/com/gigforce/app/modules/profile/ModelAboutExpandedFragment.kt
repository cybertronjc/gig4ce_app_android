package com.gigforce.app.modules.profile

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone


class ModelAboutExpandedFragment : ModelCallbacksAboutExpandedFragment,
    BaseFirestoreDBRepository() {

    override fun setIsWhatsAppNumber(
        profileID: String,
        contactList: ArrayList<ContactPhone>,
        number: String,
        isWhatsAppNumber: Boolean,
        responseCallbacks: ModelCallbacksAboutExpandedFragment.ResponseModelCallbacksAboutExpandedFragment
    ) {


        for (contact in contactList) {
            if (contact.phone == number) {
                contact.isWhatsapp = isWhatsAppNumber
                break
            }
        }
        getCollectionReference().document(profileID).update("contactPhone", contactList)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    responseCallbacks.reloadProfile(true)
                }
            }


    }

    override fun updateContactAndEmailSeparately(
        profileID: String,
        contactList: ArrayList<Contact>,
        responseCallbacks: ModelCallbacksAboutExpandedFragment.ResponseModelCallbacksAboutExpandedFragment
    ) {
        val contact = ArrayList<ContactPhone>()
        val email = ArrayList<ContactEmail>()
        contactList.forEachIndexed { index, element ->
            if (index == 0) {
                contact.add(ContactPhone(element.phone, true, isWhatsapp = false))
                email.add(ContactEmail(element.email))
            } else {
                contact.add(ContactPhone(element.phone, false, isWhatsapp = false))
                email.add(ContactEmail(element.email))
            }
        }
        val updateMap = mapOf(
            "contactPhone" to contact,
            "contactEmail" to email
        )
        getCollectionReference().document(profileID).update(updateMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    responseCallbacks.reloadProfile(false)
                }
            }
    }


    override fun getCollectionName(): String {
        return "Profiles"
    }


}

