package com.gigforce.lead_management.views

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.lead_management.ui.joining_list.views.JoiningRecyclerItemView
import com.gigforce.lead_management.ui.joining_list.views.JoiningStatusRecyclerItemView

object LeadActivationViewTypeLoader : IViewTypeLoader {

    override fun getView(
        context: Context,
        viewType: Int
    ): View? {

        return when (viewType) {
            LeadActivationViewTypes.JoiningListStatus -> JoiningStatusRecyclerItemView(
                context,
                null
            )
            LeadActivationViewTypes.JoiningList -> JoiningRecyclerItemView(
                context,
                null
            )
            else -> throw IllegalStateException("no view type match")
        }
    }
}