package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject

class VideoItemCardDVM(val image:Any,val title:String,val clockRequired:Boolean,val time:String) :
    SimpleDataViewObject(CommonViewTypes.VIEW_VIDEOS_ITEM_CARD){
}