package com.gigforce.common_ui.viewdatamodels.landing

import com.google.gson.annotations.SerializedName

data class VersionUpdateInfo(
    @SerializedName("updates") var updates : List<Updates>

) {
}

data class Updates (

    @SerializedName("version") var version : Int,
    @SerializedName("updatePriority") var updatePriority : Int

)