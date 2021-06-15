package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.giger_gigs.GigViewTypes


open class AttendanceRecyclerItemData(
        val type: Int
) : SimpleDVM(type){

    data class AttendanceRecyclerItemBusinessData(
            val businessName: String
    ) : AttendanceRecyclerItemData(
            type = GigViewTypes.ATTENDANCE_BUSSINESS_NAME
    )

    data class AttendanceRecyclerItemShiftNameData(
            val shiftName: String
    ) : AttendanceRecyclerItemData(
        type = GigViewTypes.ATTENDANCE_SHIFT_TIME
    )

    data class AttendanceRecyclerItemAttendanceData(
            val attendanceStatus: String,
            val gigId : String,
            val gigerId: String,
            val gigerName: String,
            val gigerImage : String,
            val gigerPhoneNumber: String,
            val gigerDesignation: String,
    ) : AttendanceRecyclerItemData(
        type = GigViewTypes.GIGER_ATTENDANCE
    )
}

