package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class OtherFeatureItemDVM(val image : String = "",val title : String = "",val navPath : String = "", val defaultViewType: Int = CommonViewTypes.VIEW_OTHER_FEATURE_ITEM): SimpleDVM(defaultViewType) {
}