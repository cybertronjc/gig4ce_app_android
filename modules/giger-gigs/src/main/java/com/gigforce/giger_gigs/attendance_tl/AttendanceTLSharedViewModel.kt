package com.gigforce.giger_gigs.attendance_tl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SharedAttendanceTLSharedViewModelEvents {

    data class AttendanceUpdated(
        val attendance : GigAttendanceApiModel
    ) : SharedAttendanceTLSharedViewModelEvents()

    data class OpenMarkInactiveReasonsDialog(
        val gigId : String
    ) : SharedAttendanceTLSharedViewModelEvents()
}

class AttendanceTLSharedViewModel : ViewModel() {


    private val _sharedEvents = MutableSharedFlow<SharedAttendanceTLSharedViewModelEvents>()
    val sharedEvents = _sharedEvents.asSharedFlow()

    fun attendanceUpdated(
        gigWithAttendanceUpdated: GigAttendanceApiModel
    ) = viewModelScope.launch {
        _sharedEvents.emit(SharedAttendanceTLSharedViewModelEvents.AttendanceUpdated(gigWithAttendanceUpdated))
    }

    fun openMarkInactiveReasonsDialog(
        gigId: String
    ) = viewModelScope.launch {
        _sharedEvents.emit(SharedAttendanceTLSharedViewModelEvents.OpenMarkInactiveReasonsDialog(gigId))
    }

}