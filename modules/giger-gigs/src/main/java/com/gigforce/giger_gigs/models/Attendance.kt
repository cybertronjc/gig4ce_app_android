package com.gigforce.giger_gigs.models

import android.os.Parcelable
import com.gigforce.core.SimpleDVM
import com.gigforce.giger_gigs.GigCoreRecyclerViewBindings
import com.gigforce.giger_gigs.attendance_tl.attendance_list.GigerAttendanceUnderManagerViewModel
import kotlinx.android.parcel.Parcelize


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
        var status: String,
        val statusTextColorCode : String,
        val statusBackgroundColorCode : String,

        val gigerImage: String?,
        val gigId: String,
        val gigerId: String,
        val gigerName: String,
        val gigerDesignation: String,
        val businessName: String,

        val markedByText : String,
        val lastActiveText : String,
        val hasAttendanceConflict : Boolean,
        val gigerAttendanceStatus : String,

        var currentlyMarkingAttendanceForThisGig : Boolean,

        val viewModel : GigerAttendanceUnderManagerViewModel
    ) : AttendanceRecyclerItemData(
        type = GigCoreRecyclerViewBindings.VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM
    )
}

