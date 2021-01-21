package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes

class StandardActionLblueDVM(
    image: Any?,
    title: String,
    subtitle: String,
    action: String,
    secondAction: String
) : StandardActionCardDVM(
    image,
    title,
    subtitle,
    action,
    secondAction,
    CommonViewTypes.VIEW_STANDARD_LIGHT_BLUE_WITH_MARGIN
){
}