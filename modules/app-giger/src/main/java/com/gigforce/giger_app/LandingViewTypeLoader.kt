package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.common_ui.cells.*
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.IViewTypeLoader

class LandingViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            CommonViewTypes.VIEW_STANDARD_ACTION_CARD -> StandardActionCard(context, null)
            CommonViewTypes.VIEW_STANDARD_LIGHT_PINK -> StandardActionLightPinkCard(context, null)
            CommonViewTypes.VIEW_STANDARD_LIPSTICK -> StandardActionLipstickCard(context, null)
            CommonViewTypes.VIEW_STANDARD_GREY -> StandardActionGreyCard(context, null)
            CommonViewTypes.VIEW_STANDARD_LIGHT_BLUE_WITH_MARGIN-> StandardActionMarginLblueCard(context,null)
            CommonViewTypes.VIEW_VIDEOS_LAYOUT -> VideoInfoLayout(context, null)
            CommonViewTypes.VIEW_VIDEOS_ITEM_CARD -> VideoItemCard(context, null)
            CommonViewTypes.VIEW_FEATURE_LAYOUT -> FeatureLayout(context, null)
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD -> FeatureItemCard(context, null)
            else -> null
        }
    }
}