package com.gigforce.giger_app

import android.content.Context
import android.view.View
import com.gigforce.common_ui.components.cells.SimpleCardComponent
import com.gigforce.core.IViewTypeLoader
import com.gigforce.core.datamodels.CommonViewTypes

class ComponentViewLoader: IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            CommonViewTypes.VIEW_SIMPLE_CARD -> SimpleCardComponent(context,null)
            else -> null
        }
    }
}