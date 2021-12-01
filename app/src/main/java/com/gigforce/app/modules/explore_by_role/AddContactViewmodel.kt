package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.explore_by_role.models.ContactModel
import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.datamodels.profile.ContactEmail
import com.gigforce.core.datamodels.profile.ContactPhone
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp

class AddContactViewmodel : ViewModel() {
    val repositoryAddContact = RepositoryAddContact()
    private val _observableContact: SingleLiveEvent<ProfileData> by lazy {
        SingleLiveEvent<ProfileData>();
    }
    val observableContact: SingleLiveEvent<ProfileData> get() = _observableContact
    private val _observableSetContacts: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableSetContacts: SingleLiveEvent<String> get() = _observableSetContacts


    private val _observableUpdateContact: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableUpdateContact: SingleLiveEvent<String> get() = _observableUpdateContact


    fun getPrimaryContact() {
        repositoryAddContact.getCollectionReference().document(repositoryAddContact.getUID())
            .addSnapshotListener { element, err ->
                run {
                    observableContact.value = element?.toObject(ProfileData::class.java)
                }

            }
    }

    fun addContacts(items: MutableList<ContactModel>) {
        repositoryAddContact.getCollectionReference().document(repositoryAddContact.getUID())
            .update(
                mapOf(
                    "contactPhone" to items.map { it.contactPhone },
                    "contactEmail" to items.map { it.contactEmail },
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to repositoryAddContact.getUID()
                )
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    _observableSetContacts.value = "true"
                } else {
                    _observableSetContacts.value = it.exception?.message

                }
            }

    }

    fun updateContactAndEmailSeparately(
        contactList: ArrayList<Contact>
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
            "contactEmail" to email,
            "updatedAt" to Timestamp.now(),
            "updatedBy" to repositoryAddContact.getUID()
        )
        repositoryAddContact.getCollectionReference().document(repositoryAddContact.getUID())
            .update(updateMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    observableUpdateContact.value = "true"
                } else {
                    if (it.exception != null) {
                        observableUpdateContact.value = it.exception?.message
                    }
                }
            }
    }


}