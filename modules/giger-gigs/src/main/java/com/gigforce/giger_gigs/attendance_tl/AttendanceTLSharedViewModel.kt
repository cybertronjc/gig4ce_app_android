package com.gigforce.giger_gigs.attendance_tl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SharedAttendanceTLSharedViewModelEvents {

    data class TLClickedYesInMarkActiveConfirmationDialog(
        val gigId: String
    ) : SharedAttendanceTLSharedViewModelEvents()

    data class TLSelectedInactiveReasonConfirmationDialog(
        val gigId: String,
        val reason: String
    ) : SharedAttendanceTLSharedViewModelEvents()

    data class TLSelectedResolveConfirmationDialog(
        val resolveId: String,
        val optionSelected: Boolean
    ) : SharedAttendanceTLSharedViewModelEvents()
}

class AttendanceTLSharedViewModel : ViewModel() {


    private val _sharedEvents = MutableSharedFlow<SharedAttendanceTLSharedViewModelEvents>()
    val sharedEvents = _sharedEvents.asSharedFlow()

    fun tlClickedYesInMarkActiveConfirmationDialog(
        gigId: String
    ) = viewModelScope.launch {
        _sharedEvents.emit(
            SharedAttendanceTLSharedViewModelEvents.TLClickedYesInMarkActiveConfirmationDialog(
                gigId = gigId
            )
        )
    }

    fun tlSelectedInactiveReasonConfirmationDialog(
        gigId: String,
        reason: String
    ) = viewModelScope.launch {
        _sharedEvents.emit(
            SharedAttendanceTLSharedViewModelEvents.TLSelectedInactiveReasonConfirmationDialog(
                gigId = gigId,
                reason = reason
            )
        )
    }

    fun tlSelectedYesInResolveDialog(
        resolveId: String
    ) = viewModelScope.launch {
        _sharedEvents.emit(
            SharedAttendanceTLSharedViewModelEvents.TLSelectedResolveConfirmationDialog(
                resolveId = resolveId,
                optionSelected = true
            )
        )
    }

    fun tlSelectedNoInResolveDialog(
        resolveId: String
    ) = viewModelScope.launch {
        _sharedEvents.emit(
            SharedAttendanceTLSharedViewModelEvents.TLSelectedResolveConfirmationDialog(
                resolveId = resolveId,
                optionSelected = false
            )
        )
    }

}