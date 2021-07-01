package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

class VideoItemCardDVM(
        val image: Any? = null,
        val link: String? = "",
        val thumbnail: String? = "",
        val title: String? = "",
        val clockRequired: Boolean? = true,
        val timeStr: String = "",
        val type: String? = null, //video type would be youtube or otherlink
        val navPath: String? = null,
        val length: Int = 0
) :
        SimpleDVM(CommonViewTypes.VIEW_VIDEOS_ITEM_CARD) {

    fun getVideoLength(): String {
        if (timeStr.isNotBlank()) {
            return timeStr
        } else if (length > 0) {
            val minutes = length / 60
            val secs = length % 60
            var secsStr = "$secs"
            if (secs < 10)
                secsStr = "0$secsStr"

            return "$minutes:$secsStr"
        } else return "--"
    }

}