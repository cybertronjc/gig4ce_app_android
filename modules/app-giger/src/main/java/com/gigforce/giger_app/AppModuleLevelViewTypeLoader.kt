package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.client_activation.cell.ClientActivationLayoutComponent
import com.gigforce.common_ui.molecules.FeatureItemCard2
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_app.ui.MainNavigationComponent
import com.gigforce.learning.cell.LearningLayoutComponent

class AppModuleLevelViewTypeLoader: IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when(viewType){
            AppModuleLevelViewTypes.VIEW_MAIN_NAV_CTA -> FeatureItemCard2(context, null)
            AppModuleLevelViewTypes.VIEW_MAIN_NAV_SECTION -> MainNavigationComponent(context, null)
            AppModuleLevelViewTypes.VIEW_LEARNING_SECTION -> LearningLayoutComponent(context,null)
            AppModuleLevelViewTypes.VIEW_CLIENT_ACTIVATION_SECTION->ClientActivationLayoutComponent(context,null)
            else -> null
        }
    }
}