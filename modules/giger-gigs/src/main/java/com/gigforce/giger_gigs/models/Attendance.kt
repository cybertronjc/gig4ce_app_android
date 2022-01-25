package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes


open class AttendanceRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

//    data class AttendanceRecyclerItemBusinessData(
//            val businessName: String
//    ) : AttendanceRecyclerItemData(
//            type = GigViewTypes.ATTENDANCE_BUSSINESS_NAME
//    )

    data class AttendanceRecyclerItemBusinessAndShiftNameData(
        val businessName: String,
        val enabledCount: Int,
        val activeCount: Int,
        val inActiveCount: Int,
        val expanded: Boolean
    ) : AttendanceRecyclerItemData(
        type = CommonViewTypes.VIEW_ATTENDANCE_BUSINESS_SHIFT_TIME
    )

    data class AttendanceRecyclerItemAttendanceData(
        val attendanceStatus: String,
        val gigId: String,
        val gigStatus: String,
        val gigerId: String,
        val gigerName: String,
        val gigerImage: String,
        val gigerPhoneNumber: String,
        val gigerDesignation: String,
        val gigerOffice: String,
    ) : AttendanceRecyclerItemData(
        type = CommonViewTypes.VIEW_GIGER_ATTENDANCE
    )
}

