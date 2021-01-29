package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject

class FeatureItemCard2DVM(val image:Any, val title : String, val navPath:String? = null) :
    SimpleDataViewObject(CommonViewTypes.VIEW_FEATURE_ITEM_CARD2, navPath){
}