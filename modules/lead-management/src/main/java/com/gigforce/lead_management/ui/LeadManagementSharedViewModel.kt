package com.gigforce.lead_management.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.BusinessTeamLeadersItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusinessAndJobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.ReportingLocationsItem
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class LeadManagementSharedViewModelState {

    object OnReferralDialogOkayClicked : LeadManagementSharedViewModelState()

    data class OnReferralDialogSendLinkViaLocalWhatsappClicked(
        val link : String
    ) : LeadManagementSharedViewModelState()

    data class BusinessSelected(
        val businessSelected: JoiningBusinessAndJobProfilesItem
    ): LeadManagementSharedViewModelState()

    data class JobProfileSelected(
        val businessSelected: JoiningBusinessAndJobProfilesItem,
        val jobProfileSelected: JobProfilesItem
    ): LeadManagementSharedViewModelState()

    data class CitySelected(
        val city: ReportingLocationsItem
    ): LeadManagementSharedViewModelState()

    data class ReportingLocationSelected(
        val citySelected: ReportingLocationsItem,
        val reportingLocation: ReportingLocationsItem
    ): LeadManagementSharedViewModelState()

    data class ClientTLSelected(
        val tlSelected: BusinessTeamLeadersItem
    ): LeadManagementSharedViewModelState()

    object OneOrMoreSelectionsDropped : LeadManagementSharedViewModelState()
}

class LeadManagementSharedViewModel : ViewModel() {

    private val _viewState : MutableLiveData<LeadManagementSharedViewModelState?> = MutableLiveData()
    val viewState : LiveData<LeadManagementSharedViewModelState?> = _viewState

    private val _viewStateFlow : Channel<LeadManagementSharedViewModelState> = Channel()
    val viewStateFlow = _viewStateFlow.receiveAsFlow()


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

    fun businessSelected(
        business: JoiningBusinessAndJobProfilesItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.BusinessSelected(business)
        _viewStateFlow.send(LeadManagementSharedViewModelState.BusinessSelected(business))

        delay(1000)
        _viewState.value = null
    }

    fun jobProfileSelected(
        selectedBusiness: JoiningBusinessAndJobProfilesItem,
        jobProfileSelected: JobProfilesItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.JobProfileSelected(selectedBusiness,jobProfileSelected)
        _viewStateFlow.send(LeadManagementSharedViewModelState.JobProfileSelected(selectedBusiness,jobProfileSelected))
        delay(1000)

        _viewState.value = null
    }

    fun citySelected(
        city: ReportingLocationsItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.CitySelected(city)
        _viewStateFlow.send(LeadManagementSharedViewModelState.CitySelected(city))
    }

    fun reportingLocationSelected(
        selectedCity: ReportingLocationsItem,
        reportingLocation: ReportingLocationsItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.ReportingLocationSelected(
            selectedCity,
            reportingLocation
        )
        _viewStateFlow.send(LeadManagementSharedViewModelState.ReportingLocationSelected(
            selectedCity,
            reportingLocation
        ))

        delay(1000)
        _viewState.value = null
    }

    fun clientTLSelected(
        clientTL: BusinessTeamLeadersItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.ClientTLSelected(clientTL)
        _viewStateFlow.send(LeadManagementSharedViewModelState.ClientTLSelected(clientTL))

        delay(1000)
        _viewState.value = null
    }

    fun oneOrMoreSelectionsDropped()= viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.OneOrMoreSelectionsDropped
        _viewStateFlow.send(LeadManagementSharedViewModelState.OneOrMoreSelectionsDropped)
    }
}