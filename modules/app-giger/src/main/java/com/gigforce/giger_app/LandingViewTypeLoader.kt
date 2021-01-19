package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.common_ui.cells.TipActionCard
import com.gigforce.common_ui.core.CommonViewTypes
import com.gigforce.core.IViewTypeLoader

class LandingViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when(viewType){
            CommonViewTypes.VIEW_ACTION_TIP_CARD-> TipActionCard(context,null)
            else -> null
        }
    }
}