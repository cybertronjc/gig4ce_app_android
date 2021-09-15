package com.gigforce.giger_gigs.models

import android.os.Parcelable
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
) : Parcelable {

    override fun toString(): String {
        return name
    }

}

@Parcelize
data class LoginSummaryBusiness(

    @SerializedName("business_id")
    val business_id: String = "",

    @SerializedName("businessName")
    val businessName: String = "",

    @SerializedName("legalName")
    val legalName: String = "",

    @SerializedName("jobProfileId")
    val jobProfileId: String? = null,

    @SerializedName("jobProfileName")
    val jobProfileName: String? = null,

    @SerializedName("gigerCount")
    var loginCount: Int? = null,

    var updatedBy: String? = null,

    var itemMode: Int = 0
) : Parcelable {

    override fun toString(): String {
        return businessName
    }
}

@Parcelize
data class AddNewSummaryReqModel(
    @SerializedName("UID")
    val UID: String = "",

    @SerializedName("city")
    val city: LoginSummaryCity = LoginSummaryCity(),

    @SerializedName("businessData")
    val businessData: List<BusinessDataReqModel> = emptyList(),

    @SerializedName("update")
    val update: Boolean = false,

    @SerializedName("id")
    val id: String = ""
) : Parcelable

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

    @SerializedName("jobProfileId")
    val jobProfileId: String? = null,

    @SerializedName("jobProfileName")
    val jobProfileName: String? = null,

    @SerializedName("gigerCount")
    val gigerCount: Int? = null,

    @SerializedName("updatedBy")
    val updatedBy: String? = null

) : Parcelable

@Parcelize
data class ListingTLModel(
    @SerializedName("_id")
    val _id: String = "",

    @SerializedName("UID")
    val UID: String = "",

    @SerializedName("businessData")
    val businessData: List<BusinessDataReqModel> = emptyList(),

    @SerializedName("city")
    val city: LoginSummaryCity = LoginSummaryCity(),

    @SerializedName("date")
    val date: String = "",

    @SerializedName("dateTimestamp")
    val dateTimestamp: Long,

    @SerializedName("id")
    val id: String = "",

    @SerializedName("totalPages")
    val totalPages: Int = 1

) : Parcelable


@Parcelize
data class CheckMark(
    @SerializedName("checkedIn")
    val checkedIn: Boolean = false,

    @SerializedName("gigerId")
    val gigerId: String = "",
) : Parcelable