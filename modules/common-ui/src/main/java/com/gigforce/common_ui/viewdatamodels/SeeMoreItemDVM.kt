package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

class SeeMoreItemDVM (val title : String = "",val type:String?="",val seeMoreNav:String?=""): SimpleDVM(
    CommonViewTypes.VIEW_SEE_MORE_ITEM){
}