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
    val update: Boolean? = null
)


data class BusinessDataItem(

    @field:SerializedName("newLoginToday")
    val newLoginToday: Int? = null,

    @field:SerializedName("openPositions")
    val openPositions: Int? = null,

    @field:SerializedName("expectedLoginsTomorrow")
    val expectedLoginsTomorrow: Int? = null,

    @field:SerializedName("absentToday")
    val absentToday: Int? = null,

    @field:SerializedName("city")
    var city: LoginSummaryCity? = null,

    @field:SerializedName("businessId")
    var businessId: String? = null,

    @field:SerializedName("businessName")
    var businessName: String? = null,

    @field:SerializedName("totalActive")
    val totalActive: Int? = null,

    @field:SerializedName("newOnboardingToday")
    val newOnboardingToday: Int? = null,

    @field:SerializedName("loginToday")
    val loginToday: Int? = null,

    @field:SerializedName("legalName")
    var legalName: String? = null,

    @field:SerializedName("resignedToday")
    val resignedToday: Int? = null,

    @field:SerializedName("inTrainingToday")
    val inTrainingToday: Int? = null,

    @field:SerializedName("totalLineupsForTomorrow")
    val totalLineupsForTomorrow: Int? = null,

    @field:SerializedName("jobProfileId")
    var jobProfileId: String? = null,

    @field:SerializedName("jobProfileName")
    var jobProfileName: String? = null
) {

    fun atLeastOneFieldFilled(): Boolean {
        return newLoginToday != null
                || openPositions != null
                || expectedLoginsTomorrow != null
                || absentToday != null
                || totalActive != null
                || newOnboardingToday != null
                || loginToday != null
                || resignedToday != null
                || inTrainingToday != null
                || totalLineupsForTomorrow != null
    }
}
