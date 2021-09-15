package com.gigforce.client_activation.client_activation.dataviewmodel

import com.gigforce.core.SimpleDVM
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes

class ClientActivationLayoutDVM(val image : String = "",val title : String = "",val type:String = "", val seeMoreNav:String?="",val showItem : Int = 0, var hi : HindiTranslationMapping? = null): SimpleDVM(AppModuleLevelViewTypes.VIEW_CLIENT_ACTIVATION_SECTION) {
}

open class ActionButton(
    val title: String? = "",
    val navPath: String? = "",
    val type: String? = null,
    val link: String? = null
)

open class HindiTranslationMapping(
    var action1: ActionButton? = null,
    val title: String? = "",
    var desc: String = ""
)