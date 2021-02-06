package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM


open class StandardActionCardDVM(
    val image: Int? = 0,
    val imageUrl: String? = null,
    val title: String = "",
    var desc: String = "",
    var action1: ActionButton? = null,
    var action2: ActionButton? = null,
    val bgcolor: Long = 0,
    val textColor: Int = 0,
    val marginRequired: Boolean = false,
    val defaultViewType: Int = CommonViewTypes.VIEW_STANDARD_ACTION_CARD
) : SimpleDVM(defaultViewType){

}


open class ActionButton(
    val title: String? = "",
    val navPath: String? = "",
    val type: String? = null,
    val link: String? = null
)