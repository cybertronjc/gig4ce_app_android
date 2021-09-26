package com.gigforce.common_ui.viewdatamodels

import androidx.core.os.bundleOf
import com.gigforce.core.NavArgs
import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class BannerCardDVM(
    val id:String?=null,
    val title: String? = null,
    val desc: String? = null,
    val image: String? = null,
    val apiUrl: String? = null,
    val docUrl:String? = null,
    val navPath: String? = null,
    val source : String?=null,
    val bannerName : String?=null,
    val type : String?=null,
    val index : Long?=0
) : SimpleDVM(
    CommonViewTypes.VIEW_BANNER_CARD
) {
    override fun getNavArgs(): NavArgs? {
        navPath?.let {
            return NavArgs(it, args = bundleOf("apiUrl" to apiUrl, "DOC_URL" to docUrl, "source" to source, "bannerName" to bannerName, "id" to id))
        } ?: return null
    }
}