package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class FeatureItemCard2DVM(
    val active: Boolean? = false,
    val borderCorner: Int? = 0,
    val borderShadowRadius: Int? = 0,
    val borderWidth: Int? = 0,
    val borderWidthColor: String? = "",
    val backgroundColor: String? = "",
    val title: String,
    val index: Int = 0,
    val icon: String?,
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val navPath: String? = null,
    val hi: HindiTranslationMapping? = null
) :
    SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD2, navPath)