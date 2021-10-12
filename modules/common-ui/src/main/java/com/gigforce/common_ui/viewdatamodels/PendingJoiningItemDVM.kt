package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes
import com.gigforce.common_ui.viewdatamodels.HindiTranslationMapping
import com.gigforce.core.SimpleDVM
import com.google.gson.annotations.SerializedName

class PendingJoiningItemDVM(

    @SerializedName("jobProfileId")
    val jobProfileId: String = "",

    @SerializedName("jobProfileName")
    val jobProfileName: String = "",

    @SerializedName("location")
    val location: String = "",

    @SerializedName("expectedStartDate")
    val expectedStartDate: String = "",

    @SerializedName("image")
    val image: String = ""
) : SimpleDVM(AppModuleLevelViewTypes.VIEW_PENDING_JOINING_SECTION_ITEM)