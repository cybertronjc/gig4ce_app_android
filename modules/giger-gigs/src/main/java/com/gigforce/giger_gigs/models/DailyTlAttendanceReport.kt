package com.gigforce.giger_gigs.models

import com.google.gson.annotations.SerializedName

data class DailyTlAttendanceReport(

    @field:SerializedName("UID")
    val uID: String? = null,

    @field:SerializedName("city")
    val city: LoginSummaryCity? = null,


    @field:SerializedName("businessData")
    val businessData: List<BusinessDataItem?>? = null,

    @field:SerializedName("update")
    val update: Boolean? = null,

    @field:SerializedName("id")
    val id: String? = null
)


data class BusinessDataItem(

    @field:SerializedName("newLoginToday")
    val newLoginToday: Int = 0,

    @field:SerializedName("openPositions")
    val openPositions: Int = 0,

    @field:SerializedName("expectedLoginsTomorrow")
    val expectedLoginsTomorrow: Int = 0,

    @field:SerializedName("absentToday")
    val absentToday: Int = 0,

    @field:SerializedName("city")
    var city: LoginSummaryCity? = null,

    @field:SerializedName("businessId")
    var businessId: String? = null,

    @field:SerializedName("businessName")
    var businessName: String? = null,

    @field:SerializedName("totalActive")
    val totalActive: Int = 0,

    @field:SerializedName("newOnboardingToday")
    val newOnboardingToday: Int = 0,

    @field:SerializedName("loginToday")
    val loginToday: Int = 0,

    @field:SerializedName("legalName")
    var legalName: String? = null,

    @field:SerializedName("resignedToday")
    val resignedToday: Int = 0,

    @field:SerializedName("inTrainingToday")
    val inTrainingToday: Int = 0,

    @field:SerializedName("totalLineupsForTomorrow")
    val totalLineupsForTomorrow: Int = 0,

    @field:SerializedName("jobProfileId")
    var jobProfileId: String? = null,

    @field:SerializedName("jobProfileName")
    var jobProfileName: String? = null
) {

    fun atLeastOneFieldFilled(): Boolean {
        return newLoginToday != 0
                || openPositions != 0
                || expectedLoginsTomorrow != 0
                || absentToday != 0
                || totalActive != 0
                || newOnboardingToday != 0
                || loginToday != 0
                || resignedToday != 0
                || inTrainingToday != 0
                || totalLineupsForTomorrow != 0
    }
}
