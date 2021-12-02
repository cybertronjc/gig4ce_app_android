package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.common_ui.components.atoms.OtherFeatureItemComponent
import com.gigforce.common_ui.components.atoms.SeeMoreComponent
import com.gigforce.common_ui.components.cells.*
import com.gigforce.common_ui.components.molecules.*
import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_app.ui.PendingJoiningComponent

class LandingViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            CommonViewTypes.VIEW_STANDARD_ACTION_CARD -> StandardActionCardComponent(context, null)
            CommonViewTypes.VIEW_VIDEOS_ITEM_CARD -> VideoItemCardComponent(
                context,
                null
            )
            CommonViewTypes.VIEW_FEATURE_LAYOUT -> FeatureLayoutComponent(context, null)
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD -> FeatureItemCardComponent(
                context,
                null
            )
            CommonViewTypes.VIEW_BANNER_CARD -> BannerCardComponent(
                context,
                null
            )
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD2 -> FeatureItemCard2Component(context, null)
            CommonViewTypes.VIEW_FEATURE_ITEM_CARD3 -> FeatureItemCard3Component(context,null)
            CommonViewTypes.VIEW_GIG_ITEM_CARD -> UpcomingGigCardComponent(context, null)
            CommonViewTypes.VIEW_ASSESMENT_ITEM_CARD -> AssessmentCardComponent(context, null)
            CommonViewTypes.VIEW_VIDEOS_ITEM_CARD2 -> VideoPlayCardComponent(context, null)
            CommonViewTypes.VIEW_OTHER_FEATURE -> OtherFeatureComponent(context, null)
            CommonViewTypes.VIEW_OTHER_FEATURE_ITEM -> OtherFeatureItemComponent(context, null)
            CommonViewTypes.VIEW_SEE_MORE_ITEM -> SeeMoreComponent(context, null)
            CommonViewTypes.VIEW_PENDING_JOINING_ITEM -> JoiningPendingCardComponent(context, null)

            else -> null
        }
    }
}