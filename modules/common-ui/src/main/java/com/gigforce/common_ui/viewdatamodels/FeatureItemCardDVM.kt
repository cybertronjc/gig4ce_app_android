package com.gigforce.common_ui.viewdatamodels

import android.os.Bundle
import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM

class FeatureItemCardDVM(
    val id : String? = null,
    val image : Any?,
    val title:String,
    var isSelectedView : Boolean = false,
    val subtitle:String? = null,
    val navPath:String? = null,
    val args:Bundle? = null,
    val priority:Int = 0
): SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD, navPath) {
    override fun getNavArgs(): NavArgs? {
        navPath?.let {
            return NavArgs(it,args = args)
        }?:return null
    }
}