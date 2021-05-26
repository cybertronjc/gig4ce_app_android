package com.gigforce.app.modules.gigPage2.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewState
import java.io.File

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