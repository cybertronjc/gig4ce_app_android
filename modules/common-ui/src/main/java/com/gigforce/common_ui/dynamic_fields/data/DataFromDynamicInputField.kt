package com.gigforce.common_ui.dynamic_fields.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class DataFromDynamicInputField(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("fieldType")
    val fieldType: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("value_id")
    val valueId: String? = null,

    @field:SerializedName("value")
    val value: String? = null,

) : Parcelable

@Parcelize
data class DataFromDynamicScreenField(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("fieldType")
    val fieldType: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("value_id")
    val valueId: String? = null,

    @field:SerializedName("value")
    val value: @RawValue Any? = null,

) : Parcelable