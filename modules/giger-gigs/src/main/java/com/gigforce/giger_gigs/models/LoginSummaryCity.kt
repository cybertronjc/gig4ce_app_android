package com.gigforce.giger_gigs.models

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginSummaryCity(

    @SerializedName("id")
    val id: String = "",

    @SerializedName("country_code")
    val country_code: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("state_code")
    val state_code: String = ""
)

@Parcelize
data class LoginSummaryBusiness(
    @SerializedName("id")
    val id: String,

    @SerializedName("business_id")
    val business_id: String,

    @SerializedName("businessName")
    val businessName: String,

    @SerializedName("legalName")
    val legalName: String,

    var loginCount: Int = -1
)

@Parcelize
data class AddNewSummaryReqModel(
    @SerializedName("UID")
    val UID: String = "",

    @SerializedName("city")
    val city: LoginSummaryCity = LoginSummaryCity(),

    @SerializedName("businessData")
    val businessData : List<BusinessDataReqModel> = emptyList(),

    @SerializedName("update")
    val update: Boolean = false
)

@Parcelize
data class BusinessDataReqModel(
    @SerializedName("businessId")
    val businessId: String = "",

    @SerializedName("legalName")
    val legalName: String = "",

    @SerializedName("businessName")
    val businessName: String = "",

    @SerializedName("city")
    val city: LoginSummaryCity = LoginSummaryCity(),

    @SerializedName("gigerCount")
    val gigerCount: Int = 0,
)
