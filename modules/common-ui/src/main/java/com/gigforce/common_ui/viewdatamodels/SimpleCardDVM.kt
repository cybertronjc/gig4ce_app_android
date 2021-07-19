package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class SimpleCardDVM(
    val title: String = "",
    val subtitle: String = "",
    val image: Int,
    val navpath: String = "",
    val verified: Boolean? = false,
    var isSelected: Boolean = false
) : SimpleDVM(
    CommonViewTypes.VIEW_SIMPLE_CARD
)