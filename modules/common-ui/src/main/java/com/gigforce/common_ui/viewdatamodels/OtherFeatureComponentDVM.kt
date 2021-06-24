package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

data class OtherFeatureComponentDVM(val items : List<OtherFeatureItemDVM> = emptyList(),val defaultViewType: Int = CommonViewTypes.VIEW_OTHER_FEATURE): SimpleDVM(defaultViewType){

}


