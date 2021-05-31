package com.gigforce.common_ui.viewdatamodels

import android.os.Bundle
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM

class FeatureItemCardDVM(
    val id : String? = null,
    val image : Any?,
    val title:String,
    var isSelectedView : Boolean = false,
    val subtitle:String? = null,
    val navPath:String? = null,
    val args:Bundle? = null
): SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD, navPath) {
    override fun getNavArgs(): NavArgs? {
        navPath?.let {
            return NavArgs(it,args = args)
        }?:return null
    }
}