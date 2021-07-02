package com.gigforce.learning.dataviewmodels

import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes
import com.gigforce.core.SimpleDVM

class LearningLayoutDVM(val title : String = "",val imageUrl : String = "",val type:String = ""):SimpleDVM(
    AppModuleLevelViewTypes.VIEW_LEARNING_SECTION) {
}