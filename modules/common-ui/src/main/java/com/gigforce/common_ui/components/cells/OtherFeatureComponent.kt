package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.decors.GrayColorItemDecor
import com.gigforce.common_ui.viewdatamodels.OtherFeatureComponentDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.recyclerView.CoreRecyclerView

class OtherFeatureComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    val view: View

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.other_features_layout, this, true)
    }

    override fun bind(data: Any?) {
        if(data is OtherFeatureComponentDVM){
            view.findViewById<CoreRecyclerView>(R.id.feature_item_rv).addItemDecoration(GrayColorItemDecor(true,R.color.blackLightFull,1))
            view.findViewById<CoreRecyclerView>(R.id.feature_item_rv).collection = data.items

        }
    }
}