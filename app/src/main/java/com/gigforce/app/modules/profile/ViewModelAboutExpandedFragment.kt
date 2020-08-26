package com.gigforce.app.modules.profile

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone
import com.gigforce.app.utils.SingleLiveEvent

class ViewModelAboutExpandedFragment(private val modelCallbacksAboutExpandedFragment: ModelCallbacksAboutExpandedFragment) :
    ViewModel(),
    ModelCallbacksAboutExpandedFragment.ResponseModelCallbacksAboutExpandedFragment {
    private val _observableReloadProfile: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableReloadProfile: SingleLiveEvent<Boolean> get() = _observableReloadProfile

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    fun setWhatsAppNumberStatus(
        profileID: String,
        contactList: ArrayList<ContactPhone>,
        number: String,
        isWhatsAppNumber: Boolean
    ) {
        modelCallbacksAboutExpandedFragment.setIsWhatsAppNumber(
            profileID,
            contactList,
            number,
            isWhatsAppNumber,
            this
        )
    }

    fun updateContactDetails(
        profileID: String,
        contactList: ArrayList<Contact>
    ) {
        modelCallbacksAboutExpandedFragment.updateContactAndEmailSeparately(
            profileID,
            contactList,
            this
        )

    }

    override fun reloadProfile(loadOffline: Boolean) {
        observableReloadProfile.value = loadOffline
    }

    override fun errorUpdatingContact(error: String) {
        observableError.value = error
    }

    fun contactEdit(
        profileID: String,
        contact1: String?,
        arrayList: ArrayList<ContactPhone>,
        contact: ContactPhone,
        add: Boolean
    ) {
        modelCallbacksAboutExpandedFragment.updateContact(
            profileID,
            arrayList,
            contact,
            contact1,
            add,
            this
        )

    }

    fun emailEdit(
        profileID: String,
        contact1: String?,
        arrayList: ArrayList<ContactEmail>,
        contact: ContactEmail,
        add: Boolean
    ) {
        modelCallbacksAboutExpandedFragment.updateEmail(
            profileID,
            arrayList,
            contact,
            contact1,
            add,
            this
        )
    }

    fun updateEmail(profileID: String) {
        ModelAboutExpandedFragment().updateEmails(profileID)
    }


}