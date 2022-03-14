package com.gigforce.common_ui.datamodels.attendance

import com.google.gson.annotations.SerializedName

data class GigAttendanceApiModel(

    @field:SerializedName("attendanceType")
    val attendanceType: String? = null,

    @field:SerializedName("GigerName")
    val gigerName: String? = null,

    @field:SerializedName("BusinessName")
    val businessName: String? = null,

    @field:SerializedName("tlAttendance")
    val tlAttendance: TlAttendance? = null,

    @field:SerializedName("GigerMobile")
    val gigerMobile: String? = null,

    @field:SerializedName("gigerAttedance")
    val gigerAttedance: GigerAttedance? = null,

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
    val gigerId: String? = null
) {
    fun getBusinessNameNN(): String {
        return businessName ?: "Others"
    }

    fun getFinalAttendanceStatus(): String {
        return tlAttendance?.attendanceStatus ?: gigerAttedance?.attendanceStatus ?: "Pending"
    }

    fun getFinalStatusBackgroundColorCode(): String {
        return tlAttendance?.statusBackgroundColorCode ?: gigerAttedance?.statusBackgroundColorCode
        ?: "#FFFFF"
    }

    fun getFinalStatusTextColorCode(): String {
        return tlAttendance?.statusTextColorCode ?: gigerAttedance?.statusTextColorCode ?: "#000000"
    }

    fun getLastActiveText(): String {
        return "Last active -"
    }

    fun hasAttendanceConflict() : Boolean{
        return false
    }

    fun getMarkedByText(): String {

        return if (tlAttendance != null && gigerAttedance != null) {
            "Marked by Both"
        } else if (tlAttendance != null) {
            "Marked by You"
        } else {
            "Marked by Giger"
        }
    }
}

data class FinalAttendance(

    @field:SerializedName("attendanceStatus")
    val attendanceStatus: String? = null
)

data class TlAttendance(

    @field:SerializedName("attendanceStatus")
    val attendanceStatus: String? = null,

    @field:SerializedName("statusString")
    val statusString: String? = null,

    @field:SerializedName("statusTextColorCode")
    val statusTextColorCode: String? = null,

    @field:SerializedName("statusBackgroundColorCode")
    val statusBackgroundColorCode: String? = null,

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
    val attendanceStatus: String? = null,

    @field:SerializedName("statusString")
    val statusString: String? = null,

    @field:SerializedName("statusTextColorCode")
    val statusTextColorCode: String? = null,

    @field:SerializedName("statusBackgroundColorCode")
    val statusBackgroundColorCode: String? = null,

    @field:SerializedName("checkInTime")
    val checkInTime: String? = null,

    @field:SerializedName("checkOutTime")
    val checkOutTime: String? = null,

    @field:SerializedName("checkInImage")
    val checkInImage: String? = null,

    @field:SerializedName("checkOutImage")
    val checkOutImage: String? = null
)
