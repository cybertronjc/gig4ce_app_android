package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.core.SimpleDVM

class VideoItemCardDVM(
    val image: Any? = null,
    val link: String? = "",
    val thumbnail : String? = "",
    val title: String? = "",
    val clockRequired: Boolean? = true,
    val timeSeconds: Int = 0,
    val timeStr : String = "",
    val type: String? = null, //video type would be youtube or otherlink
    val navPath:String?=null
) :
    SimpleDVM(CommonViewTypes.VIEW_VIDEOS_ITEM_CARD) {

    fun getVideoLength(): String {
        if (timeStr?.isNotBlank()?:false ) {
            return timeStr
        } else if (timeSeconds is Int) {
            if (timeSeconds >= 60) {
                val minutes = timeSeconds / 60
                val secs = timeSeconds % 60
                var secsStr = "$secs"
                if(secs<10)
                    secsStr = "0$secsStr"

                return "$minutes:$secsStr"
            } else {
                var secsStr = "$timeSeconds"
                if(timeSeconds<10)
                    secsStr = "0$secsStr"
                return "00:${secsStr}"
            }
        } else return "--"
    }

}