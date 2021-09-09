package com.gigforce.giger_app.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes
import com.gigforce.common_ui.viewdatamodels.HindiTranslationMapping

class HelpVideosSectionDVM(val title : String = "",val imageUrl : String = "",val type:String?="",val navPath:String?="",val showVideo : Int = 0, var hi : HindiTranslationMapping?=null
): SimpleDVM(AppModuleLevelViewTypes.VIEW_HELP_VIDEO_SECTION){
}