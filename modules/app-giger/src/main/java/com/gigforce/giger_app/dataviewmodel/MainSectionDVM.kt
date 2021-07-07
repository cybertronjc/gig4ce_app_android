package com.gigforce.giger_app.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes

class MainSectionDVM(val title : String = "",val imageUrl : String = "", val type:String = ""
): SimpleDVM(AppModuleLevelViewTypes.VIEW_MAIN_NAV_SECTION){

//    override fun getViewType(): Int {
//        return when(type){
//            "sec_main_nav" -> AppModuleLevelViewTypes.VIEW_MAIN_NAV_SECTION
//            else -> 0
//        }
//    }
}