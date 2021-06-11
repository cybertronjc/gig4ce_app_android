package com.gigforce.giger_gigs.models

enum class AttendanceRecyclerViewType {
    BUSINESS_NAME_ITEM,
    SHIFT_NAME_ITEM,
    ATTENDANCE_ITEM;
}

open class AttendanceRecyclerItem(
        val type: AttendanceRecyclerViewType
)

data class AttendanceRecyclerItemBusiness(
        val businessName: String
) : AttendanceRecyclerItem(
        type = AttendanceRecyclerViewType.BUSINESS_NAME_ITEM
)

data class AttendanceRecyclerItemShiftName(
        val shiftName: String
) : AttendanceRecyclerItem(
        type = AttendanceRecyclerViewType.SHIFT_NAME_ITEM
)

data class AttendanceRecyclerItemAttendance(
        val businessName: String
) : AttendanceRecyclerItem(
        type = AttendanceRecyclerViewType.ATTENDANCE_ITEM
)