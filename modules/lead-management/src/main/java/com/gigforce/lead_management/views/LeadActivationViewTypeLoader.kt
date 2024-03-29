package com.gigforce.lead_management.views

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.lead_management.ui.giger_info.views.AppCheckListRecyclerComponent
import com.gigforce.lead_management.ui.select_gig_application.views.GigAppListRecyclerItemView
import com.gigforce.lead_management.ui.select_gig_application.views.GigAppListSearchRecyclerItemView
import com.gigforce.lead_management.ui.select_gig_application.views.GigAppListStatusRecyclerItemView
import com.gigforce.lead_management.ui.joining_list.views.JoiningRecyclerItemView
import com.gigforce.lead_management.ui.joining_list.views.JoiningStatusRecyclerItemView
import com.gigforce.lead_management.ui.joining_list_2.views.Joining2BusinessRecyclerItemView
import com.gigforce.lead_management.ui.joining_list_2.views.Joining2RecyclerItemView
import com.gigforce.lead_management.ui.select_gig_application.views.NoGigApplicationFoundItemView

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
            LeadActivationViewTypes.JoiningList2 -> Joining2RecyclerItemView(
                context,
                null
            )
            LeadActivationViewTypes.JoiningBusiness -> Joining2BusinessRecyclerItemView(
                context,
                null
            )
            LeadActivationViewTypes.GigAppList -> GigAppListRecyclerItemView(
                context,
                null
            )
            LeadActivationViewTypes.GigAppListStatus -> GigAppListStatusRecyclerItemView(
                context,
                null
            )
            LeadActivationViewTypes.GigAppListSearch -> GigAppListSearchRecyclerItemView(
                context,
                null
            )
            LeadActivationViewTypes.NoGigAppsFound -> NoGigApplicationFoundItemView(
                context,
                null
            )
            LeadActivationViewTypes.GigerInfo -> AppCheckListRecyclerComponent(
                context,
                null
            )
            else -> null
        }
    }
}