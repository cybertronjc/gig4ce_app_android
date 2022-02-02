package com.gigforce.common_ui.viewdatamodels

import androidx.core.os.bundleOf
import com.gigforce.core.NavArgs

class SimpleCardDVM1(
    val id: String? = "", val label: String? = "", val navPath: String? = null
) {
    fun getNavArgs(): NavArgs? {
        return NavArgs(args = bundleOf("id" to id, "label" to label), navPath = navPath ?: "")
    }
}