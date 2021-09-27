package com.gigforce.common_ui.viewdatamodels

import android.os.Bundle
import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes
import kotlinx.android.parcel.Parcelize

data class StandardActionCardDVM(
    val image: Int? = -1,
    val imageType: String = "",
    val imageUrl: String? = null,
    val title: String = "",
    var desc: String = "",
    var action1: ActionButton? = null,
    var action2: ActionButton? = null,
    val bgcolor: Int = 0,
    val textColor: Int = 0,
    val marginRequired: Boolean = false,
    var bundle: Bundle? = null,
    var hi: HindiTranslationMapping? = null,
    val defaultViewType: Int = CommonViewTypes.VIEW_STANDARD_ACTION_CARD
) : SimpleDVM(defaultViewType)


data class ActionButton(
    val title: String? = "",
    val navPath: String? = "",
    val type: String? = null,
    val link: String? = null
)
data class HindiTranslationMapping(
    var action1: ActionButton? = null,
    val title: String? = "",
    var desc: String = ""
)