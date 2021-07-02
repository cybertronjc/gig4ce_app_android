package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.giger_gigs.GigViewTypes

data class AttendanceStatusAndCountItemData(
        val status: String,
        val attendanceCount: Int,
        val statusSelected : Boolean
)
