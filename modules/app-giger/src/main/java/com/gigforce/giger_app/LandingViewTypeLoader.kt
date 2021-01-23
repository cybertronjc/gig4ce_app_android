package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.common_ui.cells.*
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.IViewTypeLoader

class LandingViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            CommonViewTypes.VIEW_STANDARD_ACTION_CARD -> StandardActionCardComponent(context, null)
            CommonViewTypes.VIEW_STANDARD_LIGHT_PINK -> StandardActionLightPinkCardComponent(context, null)
            CommonViewTypes.VIEW_STANDARD_LIPSTICK -> StandardActionLipstickCardComponent(context, null)
            CommonViewTypes.VIEW_STANDARD_GREY -> StandardActionGreyCardComponent(context, null)
            CommonViewTypes.VIEW_STANDARD_LIGHT_BLUE_WITH_MARGIN-> StandardMarginLightblueCardComponent(context,null)
            CommonViewTypes.VIEW_VIDEOS_LAYOUT -> VideoInfoLayoutComponent(context, null)
            CommonViewTypes.VIEW_VIDEOS_ITEM_CARD -> VideoItemCardComponent(context, null)
            CommonViewTypes.VIEW_FEATURE_LAYOUT -> FeatureLayoutComponent(context, null)
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD -> FeatureItemCardComponent(context, null)
            else -> null
        }
    }
}