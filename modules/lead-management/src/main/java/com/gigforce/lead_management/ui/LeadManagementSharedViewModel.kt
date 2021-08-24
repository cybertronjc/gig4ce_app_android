package com.gigforce.lead_management.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class LeadManagementSharedViewModelState {

    object OnReferralDialogOkayClicked : LeadManagementSharedViewModelState()

    data class OnReferralDialogSendLinkViaLocalWhatsappClicked(
        val link : String
    ) : LeadManagementSharedViewModelState()
}

class LeadManagementSharedViewModel : ViewModel() {

    private val _viewState : MutableLiveData<LeadManagementSharedViewModelState?> = MutableLiveData()
    val viewState : LiveData<LeadManagementSharedViewModelState?> = _viewState

    fun referralDialogOkayClicked() {
        _viewState.value = LeadManagementSharedViewModelState.OnReferralDialogOkayClicked
        _viewState.value = null
    }

    fun referralDialogSendLinkViaLocalWhatsappClicked(
        link : String
    ) {
        _viewState.value = LeadManagementSharedViewModelState.OnReferralDialogSendLinkViaLocalWhatsappClicked(link)
        _viewState.value = null
    }

}