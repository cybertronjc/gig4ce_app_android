package com.gigforce.common_ui.components.atoms

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.OtherFeatureItemDVM
import com.gigforce.common_ui.viewdatamodels.SeeMoreItemDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SeeMoreComponent(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs),
    IViewHolder {
    val view: View
    @Inject
    lateinit var navigation : INavigation
    init {
        this.layoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        view = LayoutInflater.from(context).inflate(R.layout.see_more_view_item, this, true)
    }

    override fun bind(data: Any?) {
        if(data is SeeMoreItemDVM){
            view.setOnClickListener{
                data.seeMoreNav?.let { it1 -> navigation.navigateTo(it1) }
            }
        }
    }
}