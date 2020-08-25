package com.gigforce.app.modules.profile

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactPhone
import com.gigforce.app.utils.SingleLiveEvent

class ViewModelAboutExpandedFragment(private val modelCallbacksAboutExpandedFragment: ModelCallbacksAboutExpandedFragment) :
    ViewModel(),
    ModelCallbacksAboutExpandedFragment.ResponseModelCallbacksAboutExpandedFragment {
    private val _observableReloadProfile: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableReloadProfile: SingleLiveEvent<Boolean> get() = _observableReloadProfile

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


}