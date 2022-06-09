package com.gigforce.app.tl_work_space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class TLWorkSpaceSharedViewModelEvent {

    data class ClientIdUpdatedOfGiger(
        val gigerId: String,
        val newClientId: String
    ) : TLWorkSpaceSharedViewModelEvent()

    data class GigerDropped(
        val gigerId: String,
        val jobProfileId: String
    ) : TLWorkSpaceSharedViewModelEvent()

    data class TeamLeaderChanged(
        val gigerId: String,
        val jobProfileId: String
    ) : TLWorkSpaceSharedViewModelEvent()
}

class TLWorkSpaceSharedViewModel : ViewModel() {

    private val _sharedEvents = MutableSharedFlow<TLWorkSpaceSharedViewModelEvent>()
    val sharedEvent = _sharedEvents.asSharedFlow()

    fun setEvent(
        event: TLWorkSpaceSharedViewModelEvent
    ) = viewModelScope.launch {
        _sharedEvents.emit(event)
    }
}