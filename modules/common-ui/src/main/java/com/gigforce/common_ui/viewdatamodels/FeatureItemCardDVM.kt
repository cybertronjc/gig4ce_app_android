package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

class FeatureItemCardDVM(
    val id : String? = null,
    val image : Any?,
    val title:String,
    val subtitle:String? = null,
    val navPath:String? = null
): SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD, navPath) {
}