package com.gigforce.common_ui.viewmodels.gig

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


sealed class SharedGigViewState {

    data class UserOkayWithNotBeingInLocationRange(val distanceBetweenGigAndUser : Float) : SharedGigViewState()
}

class SharedGigViewModel : ViewModel() {

    private val _gigSharedViewModelState: MutableLiveData<SharedGigViewState?> =
            MutableLiveData()
    val gigSharedViewModelState: LiveData<SharedGigViewState?> =
            _gigSharedViewModelState

    fun userOkayWithNotBeingInLocation(
            distanceBetweenGigAndUser : Float
    ){
        _gigSharedViewModelState.value = SharedGigViewState.UserOkayWithNotBeingInLocationRange(distanceBetweenGigAndUser)
        _gigSharedViewModelState.value = null
    }
}