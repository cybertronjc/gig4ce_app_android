package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject

class FeatureItemCardDVM(
    val image : Any?,
    val title:String,
    val subtitle:String): SimpleDataViewObject(CommonViewTypes.VIEW_FEATURE_ITEM_CARD) {
}