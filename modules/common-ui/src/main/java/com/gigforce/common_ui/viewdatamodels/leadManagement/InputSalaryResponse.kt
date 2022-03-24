package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.core.retrofit.DoNotSerialize
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InputSalaryResponse(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("data")
    val data: List<InputSalaryDataItem>? = null

) : Parcelable

@Parcelize
data class InputSalaryDataItem(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("_id")
    val _id: String? = null,

//    @field:SerializedName("isActive")
//    val isActive: Boolean? = null,
//
//    @field:SerializedName("forearning")
//    val forearning: Int? = null,
//
//    @field:SerializedName("forpayout")
//    val forpayout: Int? = null,

//    @field:SerializedName("category")
//    val category: String? = null,

//    @field:SerializedName("business")
//    val business: InputSalaryBusinessItem? = null,

    @field:SerializedName("amount")
    var amount : Int = -1,

) : Parcelable

@Parcelize
data class InputSalaryBusinessItem(
    @field:SerializedName("businessId")
    val businessId: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("isActive")
    val isActive: Boolean? = null,

    @field:SerializedName("sequence")
    val sequence: Int? = null,
) : Parcelable
