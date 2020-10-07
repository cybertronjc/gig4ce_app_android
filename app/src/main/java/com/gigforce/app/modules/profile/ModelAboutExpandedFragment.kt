package com.gigforce.app.modules.profile

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone
import com.google.firebase.firestore.FieldValue


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
                } else {
                    if (it.exception != null) {
                        responseCallbacks.errorUpdatingContact(it.exception?.message!!)
                    }
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
                } else {
                    if (it.exception != null) {
                        responseCallbacks.errorUpdatingContact(it.exception?.message!!)
                    }
                }
            }
    }

    override fun updateContact(
        profileID: String,
        contactList: ArrayList<ContactPhone>?,
        newContact: ContactPhone?,
        oldContact: String?,
        add: Boolean?,
        delete: Boolean?,
        responseCallbacks: ModelCallbacksAboutExpandedFragment.ResponseModelCallbacksAboutExpandedFragment
    ) {
        if (add == true) {
            if (contactList?.indexOfFirst {
                    it.phone == newContact?.phone ?: ""
                } != -1) {
                responseCallbacks.errorUpdatingContact("Contact Already Exists!!!")
                return
            }

            getCollectionReference().document(profileID)
                .update("contactPhone", FieldValue.arrayUnion(newContact))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        contactList.add(newContact ?: ContactPhone())
                        responseCallbacks.reloadProfile(false)
                    } else {
                        if (it.exception != null) {
                            responseCallbacks.errorUpdatingContact(it.exception?.message!!)
                        }
                    }
                }
        } else {
            for (i in 0..(contactList?.size?.minus(1) ?: 0)) {
                if (contactList!![i].phone == oldContact) {
                    if (delete == true) {
                        contactList.removeAt(i)
                        break
                    } else {
                        contactList[i] = newContact ?: ContactPhone()
                        break
                    }
                }
            }
            getCollectionReference().document(profileID).update("contactPhone", contactList)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        responseCallbacks.reloadProfile(false)
                    } else {
                        if (it.exception != null) {
                            responseCallbacks.errorUpdatingContact(it.exception?.message!!)
                        }
                    }
                }
        }
    }

    override fun updateEmail(
        profileID: String,
        contactList: ArrayList<ContactEmail>?,
        newContact: ContactEmail?,
        oldContact: String?,
        add: Boolean?,
        delete: Boolean?,
        responseCallbacks: ModelCallbacksAboutExpandedFragment.ResponseModelCallbacksAboutExpandedFragment
    ) {
        if (add == true) {
            if (contactList?.indexOfFirst {
                    it.email == newContact?.email ?: ""
                } != -1) {
                responseCallbacks.errorUpdatingContact("Email Already Exists!!!")
                return
            }

            getCollectionReference().document(profileID)
                .update("contactEmail", FieldValue.arrayUnion(newContact))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        contactList.add(newContact ?: ContactEmail())
                        responseCallbacks.reloadProfile(false)
                    } else {
                        if (it.exception != null) {
                            responseCallbacks.errorUpdatingContact(it.exception?.message!!)
                        }
                    }
                }
        } else {
            for (i in 0..(contactList?.size?.minus(1) ?: 0)) {
                if (contactList!![i].email == oldContact) {
                    if (delete == true) {
                        contactList.removeAt(i)
                        break
                    } else {
                        contactList[i] = newContact ?: ContactEmail();
                        break
                    }

                }
            }
            getCollectionReference().document(profileID).update("contactEmail", contactList)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        responseCallbacks.reloadProfile(false)
                    } else {
                        if (it.exception != null) {
                            responseCallbacks.errorUpdatingContact(it.exception?.message!!)
                        }
                    }
                }
        }
    }

    fun updateEmails(profileID: String) {
        getCollectionReference().document(profileID)
            .update(
                mapOf(
                    "contactEmail" to arrayListOf<ContactEmail>(),
                    "contactPhone" to arrayListOf<ContactPhone>(),
                    "contact" to arrayListOf<Contact>()

                )
            );

    }


    override fun getCollectionName(): String {
        return "Profiles"
    }


}

