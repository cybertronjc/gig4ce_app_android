package com.gigforce.app.domain.models.tl_workspace

import com.google.gson.annotations.SerializedName

data class TLWorkSpaceSection(

    @field:SerializedName("sectionName")
    val sectionName: String,

    @field:SerializedName("index")
    val index: Int = -1
)
