package com.gigforce.giger_app.vm

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.PendingJoiningItemDVM
import com.gigforce.core.crashlytics.CrashlyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingJoiningViewModel @Inject constructor(private val repository: LeadManagementRepository) :ViewModel(){
    var state: Parcelable? = null
    private val _liveData: MutableLiveData<List<PendingJoiningItemDVM>> =
        MutableLiveData<List<PendingJoiningItemDVM>>()
    var liveData: LiveData<List<PendingJoiningItemDVM>> = _liveData

    init {
        getPendingJoinings()

    }

    private fun getPendingJoinings() = viewModelScope.launch {
        try {
            val pendingJoinigs = repository.getPendingJoinings()
            _liveData.value = pendingJoinigs
        }catch (e:Exception){
            CrashlyticsLogger.e("PendingJoiningComponent","while fetching and setting pending joining compoent",e)
        }
    }

}