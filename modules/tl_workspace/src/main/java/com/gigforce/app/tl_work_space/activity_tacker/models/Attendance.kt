package com.gigforce.app.tl_work_space.activity_tacker.models

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.GigerAttendanceUnderManagerViewModel
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceData
import com.gigforce.core.SimpleDVM
import java.time.LocalDate


open class AttendanceRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class AttendanceBusinessHeaderItemData(
        val businessName: String,
        val enabledCount: Int,
        val activeCount: Int,
        val inActiveCount: Int,
        var expanded: Boolean,
        var currentlySelectedStatus : String,
        val viewModel : GigerAttendanceUnderManagerViewModel
    ) : AttendanceRecyclerItemData(
        type = TLWorkSpaceCoreRecyclerViewBindings.VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER
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
        val showGigerAttendanceLayout : Boolean,
        var hasAttendanceConflict : Boolean,

        val gigerAttendanceStatus : String,
        val tlMarkedAttendance : String,
        val gigerAttendanceMarkingTime : String?,
        val hasTLMarkedAttendance : Boolean,

        val canTLMarkPresent : Boolean,
        val canTLMarkAbsent : Boolean,

        var resolveId : String?,
        var currentlyMarkingAttendanceForThisGig : Boolean,

        var attendanceType : String,

        val viewModel : GigerAttendanceUnderManagerViewModel
    ) : AttendanceRecyclerItemData(
        type = TLWorkSpaceCoreRecyclerViewBindings.VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM
    ){

        fun mapToGigAttendance() : GigAttendanceData {
            return GigAttendanceData(
                status = this.status,
                statusTextColorCode = this.statusTextColorCode,
                statusBackgroundColorCode = this.statusBackgroundColorCode,
                gigerImage = this.gigerImage,
                gigId = this.gigId,
                gigerId = this.gigerId,
                gigerName = this.gigerName,
                gigerDesignation = this.gigerDesignation,
                businessName = this.businessName,
                markedByText = this.markedByText,
                lastActiveText = this.lastActiveText,
                showGigerAttendanceLayout = this.showGigerAttendanceLayout,
                hasAttendanceConflict = this.hasAttendanceConflict,
                gigerAttendanceStatus = this.gigerAttendanceStatus,
                gigerAttendanceMarkingTime = this.gigerAttendanceMarkingTime,
                resolveId = this.resolveId,
                currentlyMarkingAttendanceForThisGig = this.currentlyMarkingAttendanceForThisGig,
                hasTLMarkedAttendance = this.hasTLMarkedAttendance,
                canTLMarkPresent = this.canTLMarkPresent,
                canTLMarkAbsent = this.canTLMarkAbsent,
                joiningDate = "",
                clientId = "",
                location = "",
                scoutName = "",
                businessLogo = null,
                gigerMobileNo = "",
                attendanceType = this.attendanceType,
                gigOrderId = null,
                gigDate = LocalDate.now(),
                jobProfile = null,
            )
        }
    }
}



