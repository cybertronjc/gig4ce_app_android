package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.explore_by_role.models.ContactModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.SingleLiveEvent

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
                    "contactEmail" to items.map { it.contactEmail }


                    )
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    _observableSetContacts.value = "true"
                } else {
                    _observableSetContacts.value = it.exception?.message

                }
            }

    }


}