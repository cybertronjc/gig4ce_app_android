package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

class VideoPlayCardDVM(
    var title: String? = "",
    val coverPicture: String? = "",
    val completed: Boolean = false,
    var completionProgress : Long = 0,
    var lessonTotalLength : Long = 0,
    var videoLengthString: String? = "00:00",
) : SimpleDVM(CommonViewTypes.VIEW_VIDEOS_ITEM_CARD2){
//    fun getVideoLength(): String {
//        if (timeStr?.isNotBlank()?:false ) {
//            return timeStr
//        } else if (timeSeconds is Int) {
//            if (timeSeconds >= 60) {
//                val minutes = timeSeconds / 60
//                val secs = timeSeconds % 60
//                var secsStr = "$secs"
//                if(secs<10)
//                    secsStr = "0$secsStr"
//
//                return "$minutes:$secsStr"
//            } else {
//                var secsStr = "$timeSeconds"
//                if(timeSeconds<10)
//                    secsStr = "0$secsStr"
//                return "00:${secsStr}"
//            }
//        } else return "--"
//    }
}