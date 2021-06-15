package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.giger_gigs.GigViewTypes
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModel

data class AttendanceStatusAndViewModelData(
        val statuses: AttendanceStatusAndCountItemData,
        val viewModel: GigerAttendanceUnderManagerViewModel
): SimpleDVM(GigViewTypes.ATTENDANCE_STATUS)