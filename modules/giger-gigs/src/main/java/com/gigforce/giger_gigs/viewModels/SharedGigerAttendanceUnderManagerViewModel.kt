package com.gigforce.giger_gigs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


sealed class AttendanceUnderManagerSharedViewState {

    data class GigDeclined(
            val gigId: String
    ) : AttendanceUnderManagerSharedViewState()

}

class SharedGigerAttendanceUnderManagerViewModel : ViewModel() {


    private val _attendanceUnderManagerSharedViewState: MutableLiveData<AttendanceUnderManagerSharedViewState> =
            MutableLiveData()
    val attendanceUnderManagerSharedViewState: LiveData<AttendanceUnderManagerSharedViewState> =
            _attendanceUnderManagerSharedViewState

    fun gigDeclined(
            gigId: String
    ) {

        _attendanceUnderManagerSharedViewState.value = AttendanceUnderManagerSharedViewState.GigDeclined(
                gigId = gigId
        )
    }
}