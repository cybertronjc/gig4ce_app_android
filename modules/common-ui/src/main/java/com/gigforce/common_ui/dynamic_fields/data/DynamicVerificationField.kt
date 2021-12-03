package com.gigforce.common_ui.dynamic_fields.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class DynamicVerificationField(

    /**
     * Id for fields
     */
    @field:SerializedName("id")
    override val id: String? = null,

    @field:SerializedName("title")
    override val title: String? = null,

    @field:SerializedName("mandatory")
    override val mandatory: Boolean = false,

    @field:SerializedName("fieldType")
    override val fieldType: String?,

    @field:SerializedName("screenIdToShowIn")
    override val screenIdToShowIn: String,

    @field:SerializedName("prefillText")
    override val prefillText: String? = null,

    /**
     * status of that document
     */
    var status : String = "",
    var jobProfileId : String,
    var userId : String,
) : BaseDynamicField,Parcelable
