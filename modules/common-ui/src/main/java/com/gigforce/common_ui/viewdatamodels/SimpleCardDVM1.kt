package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.SimpleDVM

import com.gigforce.core.datamodels.CommonViewTypes

class SimpleCardDVM1(
    val id : String?="", val label :String?="", val navPath : String?=null
    ): SimpleDVM(
        CommonViewTypes.VIEW_SIMPLE_CARD1
            )