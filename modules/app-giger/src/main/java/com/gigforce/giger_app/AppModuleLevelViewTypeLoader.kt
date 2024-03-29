package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.client_activation.ui.ClientActivationLayoutComponent
import com.gigforce.common_ui.components.cells.JoiningPendingCardComponent
import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_app.ui.*
import com.gigforce.learning.ui.LearningLayoutComponent

class AppModuleLevelViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            AppModuleLevelViewTypes.VIEW_MAIN_NAV_SECTION -> MainNavigationComponent(context, null)
            AppModuleLevelViewTypes.VIEW_GIGFORCE_TIP_SECTION-> GigforceTipsComponent(context,null)
            AppModuleLevelViewTypes.VIEW_LEARNING_SECTION -> LearningLayoutComponent(context, null)
            AppModuleLevelViewTypes.VIEW_CLIENT_ACTIVATION_SECTION -> ClientActivationLayoutComponent(
                context,
                null
            )
            AppModuleLevelViewTypes.VIEW_HELP_VIDEO_SECTION -> HelpVideoInfoComponent(context, null)
            AppModuleLevelViewTypes.VIEW_UPCOMING_GIG_SECTION -> UpcomingGigsComponent(
                context,
                null
            )
            AppModuleLevelViewTypes.VIEW_PENDING_JOINING_SECTION -> PendingJoiningComponent(
                context,
                null
            )
            AppModuleLevelViewTypes.VIEW_PENDING_JOINING_SECTION_ITEM -> JoiningPendingCardComponent(
                context,
                null
            )
            else -> null
        }
    }
}