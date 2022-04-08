package com.gigforce.common_ui.viewmodels.gig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


sealed class SharedGigViewState {

    data class UserOkayWithNotBeingInLocationRange(val distanceBetweenGigAndUser: Float) :
        SharedGigViewState()

    data class UserRatedGig(
        val rating: Float,
        val feedback: String?
    ) : SharedGigViewState()
}

class SharedGigViewModel : ViewModel() {

    private val _gigSharedViewModelState: MutableSharedFlow<SharedGigViewState?> =
        MutableSharedFlow()
    val gigSharedViewModelState = _gigSharedViewModelState.asSharedFlow()

    fun userOkayWithNotBeingInLocation(
        distanceBetweenGigAndUser: Float
    ) = viewModelScope.launch {

        _gigSharedViewModelState.emit(
            SharedGigViewState.UserOkayWithNotBeingInLocationRange(distanceBetweenGigAndUser)
        )
    }

    fun userRatedGig(
        rating: Float,
        feedback: String?
    ) = viewModelScope.launch {

        _gigSharedViewModelState.emit(
            SharedGigViewState.UserRatedGig(
                rating = rating,
                feedback = feedback
            )
        )

    }
}