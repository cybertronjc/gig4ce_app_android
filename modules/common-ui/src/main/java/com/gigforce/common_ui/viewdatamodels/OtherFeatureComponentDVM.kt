package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class OtherFeatureComponentDVM(val items : List<OtherFeatureItemDVM> = emptyList(),val defaultViewType: Int = CommonViewTypes.VIEW_OTHER_FEATURE): SimpleDVM(defaultViewType){

}


