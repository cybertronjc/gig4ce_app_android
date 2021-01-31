package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM


class VideoInfoLayoutDVM(val image : Any,val title: String, val allVideos:List<Any>, val loadMore:String):SimpleDVM(CommonViewTypes.VIEW_VIDEOS_LAYOUT) {
}