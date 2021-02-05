package com.gigforce.giger_app.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.giger_app.AppModuleLevelViewTypes

class MainSectionDVM(
    val type:String
): SimpleDVM(AppModuleLevelViewTypes.VIEW_MAIN_NAV_SECTION){

//    override fun getViewType(): Int {
//        return when(type){
//            "sec_main_nav" -> AppModuleLevelViewTypes.VIEW_MAIN_NAV_SECTION
//            else -> 0
//        }
//    }
}