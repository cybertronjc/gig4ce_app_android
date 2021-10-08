package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.core.retrofit.DoNotSerialize
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class JoiningBusinessAndJobProfilesItem(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("icon")
    val icon: String? = null,

    @field:SerializedName("jobProfiles")
    val jobProfiles: List<JobProfilesItem>,

    @DoNotSerialize
    var selected: Boolean = false
) : Parcelable

@Parcelize
data class JobProfilesItem(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

//    @field:SerializedName("dynamicInputFields")
    @DoNotSerialize
    val dynamicInputFields: List<JobProfileDependentDynamicInputField> = listOf(
        JobProfileDependentDynamicInputField(
            id = "dd",
            title = "Mandatory title",
            mandatory = true,
            inputType = JobProfileDependentDynamicInputField.INPUT_TYPE_NUMBER,
            maxLength = 10,
            screenIdToShowIn = "form_1",
            prefillText = "0"
        ),
        JobProfileDependentDynamicInputField(
            id = "dd",
            title = "Mandatory title",
            mandatory = false,
            inputType = JobProfileDependentDynamicInputField.INPUT_TYPE_NUMBER,
            maxLength = 0,
            screenIdToShowIn = "form_1",
            prefillText = null
        ),
        JobProfileDependentDynamicInputField(
            id = "dd",
            title = "nMandatory title",
            mandatory = true,
            inputType = JobProfileDependentDynamicInputField.INPUT_TYPE_TEXT,
            maxLength = 10,
            screenIdToShowIn = "form_2",
            prefillText = "0"
        ),
        JobProfileDependentDynamicInputField(
            id = "dd",
            title = "nMandatory title",
            mandatory = false,
            inputType = JobProfileDependentDynamicInputField.INPUT_TYPE_TEXT,
            maxLength = 0,
            screenIdToShowIn = "form_2",
            prefillText = null
        )
    ),

    @DoNotSerialize
    var selected: Boolean = false
) : Parcelable

@Parcelize
data class JobProfileDependentDynamicInputField(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("mandatory")
    val mandatory: Boolean = false,

    @field:SerializedName("inputType")
    val inputType: String? = INPUT_TYPE_TEXT,

    @field:SerializedName("id")
    val maxLength: Int = 0, //0 means no limit

    @field:SerializedName("screenIdToShowIn")
    val screenIdToShowIn: String,

    @field:SerializedName("prefillText")
    val prefillText: String? = null

) : Parcelable {


    companion object {

        @DoNotSerialize
        const val INPUT_TYPE_TEXT = "text"

        @DoNotSerialize
        const val INPUT_TYPE_NUMBER = "number"

        @DoNotSerialize
        const val INPUT_TYPE_NUMBER_WITH_DECIMAL = "number_with_decimal"
    }
}

@Parcelize
data class DataFromDynamicInputField(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("value")
    val value: String? = null,


) : Parcelable {


    companion object {

        @DoNotSerialize
        const val INPUT_TYPE_TEXT = "text"

        @DoNotSerialize
        const val INPUT_TYPE_NUMBER = "number"

        @DoNotSerialize
        const val INPUT_TYPE_NUMBER_WITH_DECIMAL = "number_with_decimal"
    }
}
