package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.core.SimpleDVM

class FeatureLayoutDVM (
        val image : Any,
        val title : String,
        val collection : List<Any>
): SimpleDVM(CommonViewTypes.VIEW_FEATURE_LAYOUT) {
}