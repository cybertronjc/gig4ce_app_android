package com.gigforce.common_ui.viewdatamodels

import androidx.core.os.bundleOf
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class BannerCardDVM(
    val bannerId: String? = null,
    val title: String? = null,
    val image: String? = null,
    val apiUrl: String? = null,
    val apiUrlRequire: Boolean? = false,
    val docUrl: String? = null,
    val navPath: String? = null,
    val source: String? = null,
    val bannerName: String? = null,
    val defaultDocTitle: String? = null,
    val hi: BannerHindiTranslation? = null,
    val type: String? = null,
    val index: Long? = 0
) : SimpleDVM(
    CommonViewTypes.VIEW_BANNER_CARD
) {
    override fun getNavArgs(): NavArgs? {
        navPath?.let {
            return NavArgs(
                it,
                args = bundleOf(
                    "apiUrl" to apiUrl,
                    "apiUrlRequire" to apiUrlRequire,
                    "DOC_URL" to docUrl,
                    "source" to source,
                    "bannerName" to bannerName,
                    "bannerId" to bannerId,
                    "title" to title,
                    "defaultDocTitle" to defaultDocTitle
                )
            )
        } ?: return null
    }
}

data class BannerHindiTranslation(
    val title: String? = null,
    val defaultDocTitle: String? = null,
    val image: String? = null
)