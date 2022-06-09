package com.gigforce.app.data.repositoriesImpl.gigs.models

import android.os.Parcelable
import com.gigforce.app.data.repositoriesImpl.gigs.GigAttendanceApiModel
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

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
    val jobProfileId: String?,
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
    var currentlyMarkingAttendanceForThisGig : Boolean,

    val currentDateInISOFormat: String? = null, //Current Date on server format - "2022-04-07T09:27:08.686Z",
    val gigEndDateInIsoFormat: String? = null, //Last Gig start Date format - "2022-04-07T09:27:08.686Z",
    val gigStartDateInIsoFormat: String? = null,
) : Parcelable {

    companion object {

        fun fromGigAttendanceApiModel(
            gigApiModel: GigAttendanceApiModel
        ): GigAttendanceData {

            return GigAttendanceData(
                status = gigApiModel.getFinalAttendanceStatus(),
                statusTextColorCode = gigApiModel.getFinalStatusTextColorCode(),
                statusBackgroundColorCode = gigApiModel.getFinalStatusBackgroundColorCode(),
                gigerImage = gigApiModel.profileAvatarName ?: gigApiModel.profilePicThumbnail ?: "",
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
                currentDateInISOFormat = gigApiModel.currentDate,
                gigEndDateInIsoFormat = gigApiModel.gigEndDate,
                gigStartDateInIsoFormat = gigApiModel.gigStartDate,
                jobProfileId =  gigApiModel.jobProfileId
            )
        }
    }

}