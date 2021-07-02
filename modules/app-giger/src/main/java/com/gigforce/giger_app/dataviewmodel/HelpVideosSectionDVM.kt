package com.gigforce.giger_app.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes

class HelpVideosSectionDVM(val title : String = "",val imageUrl : String = "",val type:String?="",val navPath:String?="",val showVideo : Int = 0
): SimpleDVM(AppModuleLevelViewTypes.VIEW_HELP_VIDEO_SECTION){
}