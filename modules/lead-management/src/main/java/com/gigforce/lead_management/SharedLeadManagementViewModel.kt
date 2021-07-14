package com.gigforce.lead_management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class LeadManagementSharedViewState {

    object LeadsChangedRefreshJoinings : LeadManagementSharedViewState()
}


class SharedLeadManagementViewModel : ViewModel() {

    private val _joiningsSharedViewState: MutableLiveData<LeadManagementSharedViewState> = MutableLiveData()
    val joiningsSharedViewState: LiveData<LeadManagementSharedViewState> = _joiningsSharedViewState

    fun leadsUpdated(){
        _joiningsSharedViewState.value = LeadManagementSharedViewState.LeadsChangedRefreshJoinings
    }

}