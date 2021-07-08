package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.SimpleDVM

data class SimpleCardDVM(val title : String = "",val subtitle:String = "",val image : String = "",val navpath: String = "", var isSelected : Boolean = false):SimpleDVM(CommonViewTypes.VIEW_SIMPLE_CARD) {
}