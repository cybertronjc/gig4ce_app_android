package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.core.SimpleDVM

class AssessmentCardDVM(
    var title: String = "",
    var completed: Boolean = false,
    var videoLengthString: String = "00:00",

) : SimpleDVM(CommonViewTypes.VIEW_ASSESMENT_ITEM_CARD){
}