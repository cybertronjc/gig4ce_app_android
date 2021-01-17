package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.CommonViewTypes
import com.gigforce.core.SimpleDataViewObject

class TipActionCardDVM(
    val title:String,
    val subtitle:String,
    val action:String
):SimpleDataViewObject(CommonViewTypes.VIEW_ACTION_TIP_CARD) {

}