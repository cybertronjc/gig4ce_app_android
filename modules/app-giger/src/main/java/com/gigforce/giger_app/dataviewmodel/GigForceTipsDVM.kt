package com.gigforce.giger_app.dataviewmodel

import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes

class GigForceTipsDVM(val type : String = "",val allTips : ArrayList<StandardActionCardDVM> = ArrayList<StandardActionCardDVM>()):SimpleDVM(
    AppModuleLevelViewTypes.VIEW_GIGFORCE_TIP_SECTION) {
}