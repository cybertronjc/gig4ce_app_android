package com.gigforce.client_activation.client_activation.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes

class ClientActivationLayoutDVM(val image : String = "",val title : String = "",val type:String = "", val seeMoreNav:String?="",val showItem : Int = 0): SimpleDVM(AppModuleLevelViewTypes.VIEW_CLIENT_ACTIVATION_SECTION) {
}