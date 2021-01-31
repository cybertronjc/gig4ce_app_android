package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

class VideoItemCardDVM(val image:Any,val title:String,val clockRequired:Boolean,val time:String) :
    SimpleDVM(CommonViewTypes.VIEW_VIDEOS_ITEM_CARD){
}