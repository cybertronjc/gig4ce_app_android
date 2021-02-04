package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

class VideoItemCardDVM(
    val image: Any? = null,
    val videoYoutubeId: String,
    val title: String,
    val clockRequired: Boolean,
    val timeSeconds: Int = 0,
    val timeStr : String = "",
    val videoType: String? = null //video type would be youtube or otherlink
) :
    SimpleDVM(CommonViewTypes.VIEW_VIDEOS_ITEM_CARD) {

    fun getYoutubeThumbNailUrl(): String {
        return "https://i3.ytimg.com/vi/$videoYoutubeId/hqdefault.jpg"
    }

    fun getYoutubeVideoPath(): String {
        return "https://www.youtube.com/watch?v=$videoYoutubeId"
    }

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