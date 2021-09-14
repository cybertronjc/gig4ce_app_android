package com.gigforce.giger_app.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes
import com.gigforce.common_ui.viewdatamodels.HindiTranslationMapping

class UpcomingGigSectionDVM (val title : String = "",val imageUrl : String = "",val type:String = "", var hi : HindiTranslationMapping?=null
): SimpleDVM(AppModuleLevelViewTypes.VIEW_UPCOMING_GIG_SECTION)