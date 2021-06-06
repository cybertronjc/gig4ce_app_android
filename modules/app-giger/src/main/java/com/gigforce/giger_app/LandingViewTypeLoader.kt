package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.common_ui.cells.*
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.common_ui.molecules.FeatureItemCard2Component
import com.gigforce.common_ui.molecules.FeatureItemCardComponent
import com.gigforce.common_ui.molecules.GigInfoCardComponent
import com.gigforce.common_ui.molecules.VideoItemCardComponent
import com.gigforce.core.IViewTypeLoader

class LandingViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            CommonViewTypes.VIEW_STANDARD_ACTION_CARD -> StandardActionCardComponent(context, null)
            CommonViewTypes.VIEW_VIDEOS_LAYOUT -> VideoInfoLayoutComponent(context, null)
            CommonViewTypes.VIEW_VIDEOS_ITEM_CARD -> VideoItemCardComponent(
                context,
                null
            )
            CommonViewTypes.VIEW_FEATURE_LAYOUT -> FeatureLayoutComponent(context, null)
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD -> FeatureItemCardComponent(
                context,
                null
            )
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD2 -> FeatureItemCard2Component(context,null)
            CommonViewTypes.VIEW_GIG_ITEM_CARD -> UpcomingGigCardComponent(context,null)
            else -> null
        }
    }
}