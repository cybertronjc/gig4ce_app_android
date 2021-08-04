package com.gigforce.giger_gigs

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_gigs.tl_login_details.views.BusinessRecyclerItemView

object LoginSummaryViewTypeLoader: IViewTypeLoader {

    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            LoginSummaryViewTypes.BusinessList -> BusinessRecyclerItemView(
                context,
                null
            )

            else -> null
        }
    }
}