package com.gigforce.common_ui.dynamic_fields.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DynamicField(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("mandatory")
    val mandatory: Boolean = false,

    @field:SerializedName("fieldType")
    val fieldType: String? = FieldTypes.TEXT_FIELD,

    @field:SerializedName("inputType")
    val inputType: String? = InputTypes.INPUT_TYPE_TEXT,

    @field:SerializedName("dataToShow")
    val dataToShow : List<DynamicFieldData> = emptyList(),
    /**
     * Max length of text can be filled in text
     * 0 means no limit
     */
    @field:SerializedName("maxLength")
    val maxLength: Int = 0,

    @field:SerializedName("screenIdToShowIn")
    val screenIdToShowIn: String,

    @field:SerializedName("prefillText")
    val prefillText: String? = null,

    //Default date format should be yyyy-mm-dd if null current date would be default selected date

    @field:SerializedName("defaultSelectedDate")
    val defaultSelectedDate: String? = null,

    @field:SerializedName("minDateAvailableForSelection")
    val minDateAvailableForSelection: String? = null,

    @field:SerializedName("maxDateAvailableForSelection")
    val maxDateAvailableForSelection: String? = null

) : Parcelable

@Parcelize
data class DynamicFieldData(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("value")
    val value: String? = null

): Parcelable{

    override fun toString(): String {
        return value ?: ""
    }
}