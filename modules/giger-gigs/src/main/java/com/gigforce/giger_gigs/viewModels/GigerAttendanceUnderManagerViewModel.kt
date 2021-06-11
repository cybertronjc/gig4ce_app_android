package com.gigforce.giger_gigs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.models.AttendanceRecyclerItem
import com.gigforce.giger_gigs.models.AttendanceStatusAndCount

class GigerAttendanceUnderManagerViewModel constructor(
        private val firebaseUserListe
): ViewModel() {

    private val _attendanceStatusesState = MutableLiveData<Lce<List<AttendanceStatusAndCount>>>()
    val attendanceStatusesState : LiveData<Lce<List<AttendanceStatusAndCount>>> = _attendanceStatusesState

    private val _attendanceState = MutableLiveData<Lce<List<AttendanceRecyclerItem>>>()
    val attendanceState : LiveData<Lce<List<AttendanceStatusAndCount>>> = _attendanceStatusesState



}
