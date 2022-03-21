package com.gigforce.common_ui.datamodels.attendance

import com.gigforce.common_ui.ext.nonEmptyStringOrNull
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceStatus
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceType
import com.google.gson.annotations.SerializedName

data class GigAttendanceApiModel(

    @field:SerializedName("attendanceType")
    val attendanceType: String? = null,

    @field:SerializedName("GigerName")
    val gigerName: String? = null,


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
    var isResolved: Boolean? = false,

    @field:SerializedName("resolveAttendanceId")
    val resolveAttendanceId: String? = null,

    @field:SerializedName("joiningDate")
    val joiningDate: String? = null,

    @field:SerializedName("location")
    val location: GigLocation? = null,

    @field:SerializedName("clientId")
    val clientId: String? = null,

    @field:SerializedName("scout")
    val scout: GigersScout? = null
) {


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

        if (attendanceType == null || attendanceType == AttendanceType.OVERWRITE_BOTH) {
            return getGigerMarkedAttendance()
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
                ?: getDefaultBackgroundColorCodeForStatus(AttendanceStatus.PENDING)
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
                ?: getDefaultTextColorCodeForStatus(AttendanceStatus.PENDING)
        }
    }

    fun getLastActiveText(): String {
        return "Last active -"
    }

    fun hasAttendanceConflict(): Boolean {
        return isResolved != null && !isResolved!!
    }

    fun getMarkedByText(): String {

        if (attendanceType == null || attendanceType == AttendanceType.OVERWRITE_BOTH) {

            return if (getFinalAttendanceStatus() != AttendanceStatus.PENDING) {

                if (hasTLMarkedAttendance()) {
                    "Marked by TL"
                } else {
                    "Marked by giger"
                }
            } else {
                ""
            }

        } else {

            return if (hasTLMarkedAttendance() &&
                hasGigerMarkedAttendance()
            ) {
                "Marked by both"
            } else if (hasTLMarkedAttendance()) {
                "Marked by TL"
            } else if (hasGigerMarkedAttendance()) {
                "Marked by Giger"
            } else {
                ""
            }
        }
    }

    fun canTLMarkPresent(): Boolean {

        return if (attendanceType == null || AttendanceType.OVERWRITE_BOTH == attendanceType) {
            getFinalAttendanceStatus() != AttendanceStatus.PRESENT
        } else {
            getTLMarkedAttendance() != AttendanceStatus.PRESENT
        }
    }

    fun canTLMarkAbsent(): Boolean {
        return if (attendanceType == null || AttendanceType.OVERWRITE_BOTH == attendanceType) {
            getFinalAttendanceStatus() != AttendanceStatus.ABSENT
        } else {
            getTLMarkedAttendance() != AttendanceStatus.ABSENT
        }
    }

    fun getDefaultBackgroundColorCodeForStatus(
        status: String?
    ): String {
        return when (status) {
            AttendanceStatus.PRESENT -> "#33B642"
            AttendanceStatus.ABSENT -> "#E11900"
            else -> "#FFC043"
        }
    }

    fun getDefaultTextColorCodeForStatus(
        status: String?
    ): String {
        return when (status) {
            AttendanceStatus.PRESENT -> "#FFFFFF"
            AttendanceStatus.ABSENT -> "#FFFFFF"
            else -> "#FFFFFF"
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
){

    fun createNewCopyFromGigerAttendance(
        gigerAttedance: GigerAttedance
    ) : TlAttendance{

       return  TlAttendance(
            attendanceStatus = gigerAttedance.attendanceStatus,
            status = gigerAttedance.status,
            statusString = gigerAttedance.statusString,
            statusTextColorCode = gigerAttedance.statusTextColorCode,
            statusBackgroundColorCode = gigerAttedance.statusBackgroundColorCode,
            checkInTime = gigerAttedance.checkInTime,
            checkOutTime = gigerAttedance.checkOutTime,
            checkInImage = gigerAttedance.checkInTime,
            checkOutImage = gigerAttedance.checkOutTime
        )
    }
}

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

