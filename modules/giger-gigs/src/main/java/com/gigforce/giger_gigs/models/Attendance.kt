package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.giger_gigs.GigCoreRecyclerViewBindings
import com.gigforce.giger_gigs.attendance_tl.GigerAttendanceUnderManagerViewModel


open class AttendanceRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class AttendanceBusinessHeaderItemData(
        val businessName: String,
        val enabledCount: Int,
        val activeCount: Int,
        val inActiveCount: Int,
        var expanded: Boolean,
        val viewModel : GigerAttendanceUnderManagerViewModel
    ) : AttendanceRecyclerItemData(
        type = GigCoreRecyclerViewBindings.VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER
    )

    data class AttendanceRecyclerItemAttendanceData(
        val status: String,
        val statusTextColorCode : String,
        val statusBackgroundColorCode : String,

        val gigerImage: String,
        val gigId: String,
        val gigerId: String,
        val gigerName: String,
        val gigerDesignation: String,

        val markedByText : String,
        val lastActiveText : String,
        val hasAttendanceConflict : Boolean,
        val gigerAttendanceStatus : String,

        val viewModel : GigerAttendanceUnderManagerViewModel
    ) : AttendanceRecyclerItemData(
        type = GigCoreRecyclerViewBindings.VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM
    )
}

