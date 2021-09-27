package com.gigforce.common_ui.viewdatamodels

import androidx.core.os.bundleOf
import com.gigforce.core.NavArgs
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
    val index: Long = 1000,
    val icon: String? = "",
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val navPath: String? = null,
    val hi: HindiTranslationMapping? = null,
    val type: String? = null,
    val subicons: List<Long>? = null
) :
    SimpleDVM(CommonViewTypes.VIEW_FEATURE_ITEM_CARD2, navPath) {
    override fun getNavArgs(): NavArgs? {
        navPath?.let {
            return NavArgs(navPath, bundleOf("title" to title))
        }
        return null
    }

}