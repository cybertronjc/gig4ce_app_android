package com.gigforce.lead_management.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    data class ReportingTLSelected(
        val tlSelected: TeamLeader
    ): LeadManagementSharedViewModelState()

    object OneOrMoreSelectionsDropped : LeadManagementSharedViewModelState()
    object JoiningAdded: LeadManagementSharedViewModelState()
}

class LeadManagementSharedViewModel : ViewModel() {

    private val _viewState : MutableLiveData<LeadManagementSharedViewModelState?> = MutableLiveData()
    val viewState : LiveData<LeadManagementSharedViewModelState?> = _viewState

    private val _viewStateFlow : MutableSharedFlow<LeadManagementSharedViewModelState> = MutableSharedFlow()
    val viewStateFlow = _viewStateFlow.asSharedFlow()


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
        _viewStateFlow.emit(LeadManagementSharedViewModelState.BusinessSelected(business))

        delay(1000)
        _viewState.value = null
    }

    fun jobProfileSelected(
        selectedBusiness: JoiningBusinessAndJobProfilesItem,
        jobProfileSelected: JobProfilesItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.JobProfileSelected(selectedBusiness,jobProfileSelected)
        delay(200)
        _viewState.value = null

        _viewStateFlow.emit(LeadManagementSharedViewModelState.JobProfileSelected(selectedBusiness,jobProfileSelected))
    }

    fun citySelected(
        city: ReportingLocationsItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.CitySelected(city)
        _viewStateFlow.emit(LeadManagementSharedViewModelState.CitySelected(city))
    }

    fun reportingLocationSelected(
        selectedCity: ReportingLocationsItem,
        reportingLocation: ReportingLocationsItem
    ) = viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.ReportingLocationSelected(
            selectedCity,
            reportingLocation
        )
        _viewStateFlow.emit(LeadManagementSharedViewModelState.ReportingLocationSelected(
            selectedCity,
            reportingLocation
        ))

        delay(1000)
        _viewState.value = null
    }

    fun reportingTLSelected(
        tl: TeamLeader
    ) = viewModelScope.launch{
        _viewStateFlow.emit(LeadManagementSharedViewModelState.ReportingTLSelected(tl))
    }

    fun oneOrMoreSelectionsDropped()= viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.OneOrMoreSelectionsDropped
        _viewStateFlow.emit(LeadManagementSharedViewModelState.OneOrMoreSelectionsDropped)
    }
    fun joiningAdded()= viewModelScope.launch{
        _viewState.value = LeadManagementSharedViewModelState.JoiningAdded
        _viewStateFlow.emit(LeadManagementSharedViewModelState.JoiningAdded)
    }
}