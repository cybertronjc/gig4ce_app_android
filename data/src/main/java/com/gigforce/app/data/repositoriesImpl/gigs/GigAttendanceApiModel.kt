package com.gigforce.app.data.repositoriesImpl.gigs

import com.gigforce.app.data.repositoriesImpl.gigs.models.GigLocation
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigersScout
import com.gigforce.core.extensions.nonEmptyStringOrNull
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GigAttendanceApiModel(

    @field:SerializedName("attendanceType")
    val attendanceType: String? = null,

    @field:SerializedName("GigerName")
    val gigerName: String? = null,

    @field:SerializedName("profilePicThumbnail")
    val profilePicThumbnail: String? = null,

    @field:SerializedName("profileAvatarName")
    val profileAvatarName: String? = null,

    @field: SerializedName("businessIcon")
    val businessIcon: String? = null,

    @field:SerializedName("BusinessName")
    val businessName: String? = null,

    @field:SerializedName("tlAttendance")
    var tlAttendance: TlAttendance? = null,


    @field:SerializedName("GigerMobile")
    val gigerMobile: String? = null,

    @field:SerializedName("gigerAttedance")
    var gigerAttedance: GigerAttedance? = null,

    @field:SerializedName("gigDate")
    val gigDate: String? = null,

    @field:SerializedName("finalAttendance")
    val finalAttendance: FinalAttendance? = null,

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("JobProfile")
    val jobProfile: String? = null,

    @field:SerializedName("BusinessId")
    val businessId: String? = null,

    @field:SerializedName("GigerId")
    val gigerId: String? = null,

    @field:SerializedName("isResolved")
    var isResolved: Boolean? = null,

    @field:SerializedName("resolveAttendanceId")
    val resolveAttendanceId: String? = null,

    @field:SerializedName("joiningDate")
    val joiningDate: String? = null,

    @field:SerializedName("location")
    val location: GigLocation? = null,

    @field:SerializedName("clientId")
    val clientId: String? = null,

    @field:SerializedName("scout")
    val scout: GigersScout? = null,

    @field:SerializedName("GigOrderId")
    val gigOrderId: String? = null,

    @field:SerializedName("lastActiveString")
    val lastActiveString: String? = null,

    @field:SerializedName("currentDate")
    val currentDate: String? = null, //Current Date on server format - "2022-04-07T09:27:08.686Z",

    @field:SerializedName("gigEndDate")
    val gigEndDate: String? = null, //Last Gig start Date format - "2022-04-07T09:27:08.686Z",

    @field:SerializedName("gigStartDate")
    val gigStartDate: String? = null, //First gig Start date format - "2022-04-07T09:27:08.686Z"
) {

    fun getProfilePicture() : String{
        return profilePicThumbnail ?: profileAvatarName ?: return ""
    }


    fun getAttendanceTypeNN() : String{
        if (attendanceType.isNullOrBlank() || AttendanceType.OVERWRITE_BOTH == attendanceType) {
            return AttendanceType.OVERWRITE_BOTH
        } else{
            return AttendanceType.PARALLEL_ONLY_TL
        }
    }

    fun getTLMarkedAttendance(): String {
        if (attendanceType != null && attendanceType == AttendanceType.PARALLEL_ONLY_TL) {
            return tlAttendance?.status ?: AttendanceStatus.PENDING
        } else {
            return if ("Manager" == gigerAttedance?.markedBy) {
                gigerAttedance?.status ?: AttendanceStatus.PENDING
            } else {
                AttendanceStatus.PENDING
            }
        }
    }

    fun getGigerMarkedAttendance(): String {
        if (attendanceType != null && attendanceType == AttendanceType.PARALLEL_ONLY_TL) {
            return gigerAttedance?.status ?: AttendanceStatus.PENDING
        } else {
            return if ("Manager" != gigerAttedance?.markedBy) {
                gigerAttedance?.status ?: AttendanceStatus.PENDING
            } else {
                AttendanceStatus.PENDING
            }
        }
    }

    fun getGigerMarkedAttendanceIgnoreMarkedBy(): String{
        return gigerAttedance?.status ?: AttendanceStatus.PENDING
    }

    fun hasTLMarkedAttendance(): Boolean {
        return getTLMarkedAttendance() != AttendanceStatus.PENDING
    }

    fun hasGigerMarkedAttendance(): Boolean {
        return getGigerMarkedAttendance() != AttendanceStatus.PENDING
    }

    fun hasUserAndTLMarkedDifferentAttendance() =
        if (attendanceType != null && attendanceType == AttendanceType.PARALLEL_ONLY_TL) {

            if (hasTLMarkedAttendance() && hasGigerMarkedAttendance()) {
                getTLMarkedAttendance() != getGigerMarkedAttendance()
            } else {
                false
            }
        } else {
            false
        }


    fun getBusinessNameNN(): String {
        return businessName ?: "Others"
    }

    fun getFinalAttendanceStatus(): String {

        if (attendanceType.isNullOrBlank() || attendanceType == AttendanceType.OVERWRITE_BOTH) {
            return getGigerMarkedAttendanceIgnoreMarkedBy()
        } else {

            return if (getTLMarkedAttendance() != AttendanceStatus.PENDING) {
                getTLMarkedAttendance()
            } else if (getGigerMarkedAttendance() != AttendanceStatus.PENDING) {
                getGigerMarkedAttendance()
            } else {
                AttendanceStatus.PENDING
            }
        }
    }

    fun getFinalStatusBackgroundColorCode(): String {

        if (AttendanceType.PARALLEL_ONLY_TL == attendanceType) {

            return if (hasTLMarkedAttendance()) {
                tlAttendance?.statusBackgroundColorCode.nonEmptyStringOrNull()
                    ?: getDefaultBackgroundColorCodeForStatus(
                        tlAttendance?.status
                    )
            } else if (hasGigerMarkedAttendance()) {
                gigerAttedance?.statusBackgroundColorCode.nonEmptyStringOrNull()
                    ?: getDefaultBackgroundColorCodeForStatus(
                        gigerAttedance?.status
                    )
            } else {
                tlAttendance?.statusBackgroundColorCode.nonEmptyStringOrNull()
                    ?: getDefaultBackgroundColorCodeForStatus(AttendanceStatus.PENDING)
            }

        } else {

            return gigerAttedance?.statusBackgroundColorCode.nonEmptyStringOrNull()
                ?: if (!gigerAttedance?.status.isNullOrBlank()) {
                    getDefaultBackgroundColorCodeForStatus(gigerAttedance?.status)
                } else {
                    getDefaultBackgroundColorCodeForStatus(AttendanceStatus.PENDING)
                }
        }
    }

    fun getFinalStatusTextColorCode(): String {

        if (AttendanceType.PARALLEL_ONLY_TL == attendanceType) {

            return if (hasTLMarkedAttendance()) {
                tlAttendance?.statusTextColorCode.nonEmptyStringOrNull()
                    ?: getDefaultTextColorCodeForStatus(
                        tlAttendance?.status
                    )
            } else if (hasGigerMarkedAttendance()) {
                gigerAttedance?.statusTextColorCode.nonEmptyStringOrNull()
                    ?: getDefaultTextColorCodeForStatus(
                        gigerAttedance?.status
                    )
            } else {
                tlAttendance?.statusTextColorCode.nonEmptyStringOrNull()
                    ?: getDefaultTextColorCodeForStatus(AttendanceStatus.PENDING)
            }

        } else {

            return gigerAttedance?.statusTextColorCode.nonEmptyStringOrNull()
                ?: if (!gigerAttedance?.status.isNullOrBlank()) {
                    getDefaultTextColorCodeForStatus(gigerAttedance?.status)
                } else {
                    getDefaultTextColorCodeForStatus(AttendanceStatus.PENDING)
                }
        }
    }

    fun getLastActiveText(): String {
        return lastActiveString ?: "Last active : N/A"
    }

    fun hasAttendanceConflict(): Boolean {
        return isResolved != null && !isResolved!!
    }

    fun getMarkedByText(): String {

        if (attendanceType == null || attendanceType == AttendanceType.OVERWRITE_BOTH) {

            return if (getFinalAttendanceStatus() != AttendanceStatus.PENDING) {

                if (hasTLMarkedAttendance()) {
                    "Marked by You"
                } else {
                    "Marked by giger"
                }
            } else {
                ""
            }
        } else {

            return if (
                hasTLMarkedAttendance() &&
                hasGigerMarkedAttendance()
            ) {
                if(getTLMarkedAttendance() == getGigerMarkedAttendance()){
                    "Marked by both"
                } else{
                    "Marked by You"
                }
            } else if (hasTLMarkedAttendance()) {
                "Marked by You"
            } else if (hasGigerMarkedAttendance()) {
                "Marked by Giger"
            } else {
                ""
            }
        }
    }

    fun getGigDate() : LocalDate{
       return LocalDate.parse(
           gigDate!!,
           DateTimeFormatter.ISO_DATE
       )
    }

    fun canTLMarkPresent(): Boolean {

        return if (attendanceType == null || AttendanceType.OVERWRITE_BOTH == attendanceType) {
            getFinalAttendanceStatus() != AttendanceStatus.PRESENT && getGigDate() == LocalDate.now()
        } else {
            getTLMarkedAttendance() != AttendanceStatus.PRESENT && getGigDate() == LocalDate.now()
        }
    }

    fun canTLMarkAbsent(): Boolean {
        return if (attendanceType == null || AttendanceType.OVERWRITE_BOTH == attendanceType) {
            getFinalAttendanceStatus() != AttendanceStatus.ABSENT && !getGigDate().isBefore(LocalDate.now())
        } else {
            getTLMarkedAttendance() != AttendanceStatus.ABSENT && !getGigDate().isBefore(LocalDate.now())
        }
    }

    private fun getDefaultBackgroundColorCodeForStatus(
        status: String?
    ): String {
        return when (status) {
            AttendanceStatus.PRESENT -> "#33B642"
            AttendanceStatus.ABSENT -> "#E11900"
            else -> "#CDCFD0"
        }
    }

    private fun getDefaultTextColorCodeForStatus(
        status: String?
    ): String {
        return when (status) {
            AttendanceStatus.PRESENT -> "#FFFFFF"
            AttendanceStatus.ABSENT -> "#FFFFFF"
            else -> "#000000"
        }
    }

}

data class FinalAttendance(

    @field:SerializedName("attendanceStatus")
    val attendanceStatus: String? = null
)

data class TlAttendance(

    @field:SerializedName("attendanceStatus")
    var attendanceStatus: String? = null,

    @field:SerializedName("status")
    var status: String? = null,

    @field:SerializedName("statusString")
    var statusString: String? = null,

    @field:SerializedName("statusTextColorCode")
    var statusTextColorCode: String? = null,

    @field:SerializedName("statusBackgroundColorCode")
    var statusBackgroundColorCode: String? = null,

    @field:SerializedName("checkInTime")
    val checkInTime: String? = null,

    @field:SerializedName("checkOutTime")
    val checkOutTime: String? = null,

    @field:SerializedName("checkInImage")
    val checkInImage: String? = null,

    @field:SerializedName("checkOutImage")
    val checkOutImage: String? = null
)

data class GigerAttedance(

    @field:SerializedName("attendanceStatus")
    var attendanceStatus: String? = null,

    @field:SerializedName("status")
    var status: String? = null,

    @field:SerializedName("markedBy")
    var markedBy: String? = null,

    @field:SerializedName("statusString")
    var statusString: String? = null,

    @field:SerializedName("statusTextColorCode")
    var statusTextColorCode: String? = null,

    @field:SerializedName("statusBackgroundColorCode")
    var statusBackgroundColorCode: String? = null,

    @field:SerializedName("checkInTime")
    val checkInTime: String? = null,

    @field:SerializedName("checkOutTime")
    val checkOutTime: String? = null,

    @field:SerializedName("checkInImage")
    val checkInImage: String? = null,

    @field:SerializedName("checkOutImage")
    val checkOutImage: String? = null
)

