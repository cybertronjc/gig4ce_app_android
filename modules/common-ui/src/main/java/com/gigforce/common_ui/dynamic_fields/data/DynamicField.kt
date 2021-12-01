package com.gigforce.common_ui.dynamic_fields.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class DynamicField(

    /**
     * Id for fields
     */
    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    /**
     * Is field mandatory, user can't process if he doesn't enter or select this field
     */
    @field:SerializedName("mandatory")
    val mandatory: Boolean = false,

    /**
     * Type of field to inflate, currently allowed options
     *
     * [FieldTypes.TEXT_FIELD]
     * [FieldTypes.DROP_DOWN]
     * [FieldTypes.DATE_PICKER]
     * [FieldTypes.RADIO_BUTTON]
     * [FieldTypes.SIGNATURE_DRAWER]
     * [FieldTypes.SIGNATURE_DRAWER_2]
     * [FieldTypes.AADHAAR_VERIFICATION_VIEW]
     * [FieldTypes.BANK_VERIFICATION_VIEW]
     * [FieldTypes.DL_VERIFICATION_VIEW]
     * [FieldTypes.PAN_VERIFICATION_VIEW]
     */
    @field:SerializedName("fieldType")
    val fieldType: String? = FieldTypes.TEXT_FIELD,

    /**
     * Type of input to to take from user, only works for [FieldTypes.TEXT_FIELD]
     *
     * [InputTypes.INPUT_TYPE_TEXT]
     * [InputTypes.INPUT_TYPE_NUMBER]
     * [InputTypes.INPUT_TYPE_NUMBER_WITH_DECIMAL]
     */
    @field:SerializedName("inputType")
    val inputType: String? = InputTypes.INPUT_TYPE_TEXT,

    /**
     * Max length of text can be filled in text, only works for [FieldTypes.TEXT_FIELD]
     * 0 means no limit
     */
    @field:SerializedName("maxLength")
    val maxLength: Int = 0,

    /**
     * Screen to this field in currently can be Joining forms
     */
    @field:SerializedName("screenIdToShowIn")
    val screenIdToShowIn: String,

    /**
     * Text to prefill, only works for [FieldTypes.TEXT_FIELD]
     */
    @field:SerializedName("prefillText")
    val prefillText: String? = null,


    /**
     * Default date to be selected on start, only works for [FieldTypes.DATE_PICKER]
     *
     * allowed options
     * * null - no date is selected
     * * today - current date
     * * date in YYYY-MM-DD format
     */
    @field:SerializedName("defaultSelectedDate")
    val defaultSelectedDate: String? = null,

    /**
     * Minimum date user can select, only works for [FieldTypes.DATE_PICKER]
     *
     * allowed options
     * * null - no date is selected
     * * today - current date
     * * date in YYYY-MM-DD format
     */
    @field:SerializedName("minDateAvailableForSelection")
    val minDateAvailableForSelection: String? = null,

    /**
     * Maximum date user can select, only works for [FieldTypes.DATE_PICKER]
     *
     * allowed options
     * * null - no date is selected
     * * today - current date
     * * date in YYYY-MM-DD format
     */
    @field:SerializedName("maxDateAvailableForSelection")
    val maxDateAvailableForSelection: String? = null,

    /**
     * Data to show on fields works only works for [FieldTypes.RADIO_BUTTON], [FieldTypes.DROP_DOWN]
     */
    @field:SerializedName("data")
    val data: List<DynamicFieldData>? = emptyList()

) : Parcelable

@Parcelize
data class DynamicFieldData(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("value")
    val value: String? = null

) : Parcelable {

    override fun toString(): String {
        return value ?: ""
    }
}