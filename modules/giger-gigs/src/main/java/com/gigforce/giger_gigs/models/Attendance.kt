package com.gigforce.giger_gigs.models

import android.os.Parcelable
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.core.SimpleDVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_gigs.GigCoreRecyclerViewBindings
import com.gigforce.giger_gigs.attendance_tl.attendance_list.GigerAttendanceUnderManagerViewModel
import com.gigforce.giger_gigs.repositories.GigersAttendanceRepository
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import kotlin.random.Random


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
        type = GigCoreRecyclerViewBindings.VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM
    ){

        fun mapToGigAttendance() : GigAttendanceData{
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

@Parcelize
data class GigAttendanceData(
    var status: String,
    val attendanceType : String,
    val statusTextColorCode : String,
    val statusBackgroundColorCode : String,

    val gigerImage: String?,
    val gigId: String,
    val gigerId: String,
    val gigerName: String,
    val gigerMobileNo : String?,
    val gigerDesignation: String,
    val gigOrderId: String?,
    val gigDate: LocalDate,
    val jobProfile: String?,

    val businessLogo : String?,
    val businessName: String,

    val joiningDate: String,
    val clientId: String,
    val location: String,
    val scoutName: String,

    val markedByText : String,
    val lastActiveText : String,
    val showGigerAttendanceLayout : Boolean,
    val hasAttendanceConflict : Boolean,
    val hasTLMarkedAttendance : Boolean,
    val canTLMarkPresent : Boolean,
    val canTLMarkAbsent : Boolean,

    val gigerAttendanceStatus : String?,
    val gigerAttendanceMarkingTime : String?,

    var resolveId : String?,
    var currentlyMarkingAttendanceForThisGig : Boolean
) : Parcelable{

    companion object {

        fun fromGigAttendanceApiModel(
            gigApiModel: GigAttendanceApiModel
        ): GigAttendanceData {

            return GigAttendanceData(
                status = gigApiModel.getFinalAttendanceStatus(),
                statusTextColorCode = gigApiModel.getFinalStatusTextColorCode(),
                statusBackgroundColorCode = gigApiModel.getFinalStatusBackgroundColorCode(),
                gigerImage = gigApiModel.getProfilePicture(),
                gigId = gigApiModel.id ?: "",
                gigerId = gigApiModel.gigerId ?: "",
                gigerName = gigApiModel.gigerName ?: "",
                gigerDesignation = gigApiModel.jobProfile ?: "",

                businessLogo = gigApiModel.businessIcon,
                businessName = gigApiModel.businessName ?: "",

                markedByText = gigApiModel.getMarkedByText(),
                lastActiveText = gigApiModel.getLastActiveText(),
                showGigerAttendanceLayout = gigApiModel.hasUserAndTLMarkedDifferentAttendance(),
                hasAttendanceConflict = gigApiModel.hasAttendanceConflict(),
                hasTLMarkedAttendance = gigApiModel.hasTLMarkedAttendance(),
                gigerAttendanceStatus = gigApiModel.getGigerMarkedAttendance(),
                gigerAttendanceMarkingTime = gigApiModel.gigerAttedance?.checkInTime,
                resolveId = gigApiModel.resolveAttendanceId,
                currentlyMarkingAttendanceForThisGig = false,
                joiningDate = gigApiModel.joiningDate ?: "",
                clientId = gigApiModel.clientId ?: "",
                location = gigApiModel.location?.name?: "",
                scoutName = gigApiModel.scout?.name ?: "",
                canTLMarkPresent = gigApiModel.canTLMarkPresent(),
                canTLMarkAbsent = gigApiModel.canTLMarkAbsent(),
                gigerMobileNo = gigApiModel.gigerMobile,
                attendanceType = gigApiModel.getAttendanceTypeNN(),
                gigOrderId = gigApiModel.gigOrderId,
                gigDate = gigApiModel.getGigDate(),
                jobProfile = gigApiModel.jobProfile,
            )
        }
    }

}

