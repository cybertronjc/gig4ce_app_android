package com.gigforce.common_ui.viewdatamodels

import androidx.core.os.bundleOf
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class BannerCardDVM(
    val title: String? = null,
    val desc: String? = null,
    val image: String? = null,
    val url: String? = null,
    val navPath: String? = null
) : SimpleDVM(
    CommonViewTypes.VIEW_FEATURE_ITEM_CARD
) {
    override fun getNavArgs(): NavArgs? {
        navPath?.let {
            return NavArgs(it, args = bundleOf("url" to url))
        } ?: return null
    }
}