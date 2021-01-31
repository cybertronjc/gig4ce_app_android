package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

class FeatureLayoutDVM (
        val image : Any,
        val title : String,
        val collection : List<Any>
): SimpleDVM(CommonViewTypes.VIEW_FEATURE_LAYOUT) {
}