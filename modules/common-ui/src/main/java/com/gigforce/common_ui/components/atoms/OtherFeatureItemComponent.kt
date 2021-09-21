package com.gigforce.common_ui.components.atoms

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.OtherFeatureItemDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.other_feature_item_layout.view.*
import javax.inject.Inject

@AndroidEntryPoint
class OtherFeatureItemComponent(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs),
    IViewHolder {
    val view: View
    @Inject lateinit var navigation : INavigation
    @Inject lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    init {
        this.layoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        view = LayoutInflater.from(context).inflate(R.layout.other_feature_item_layout, this, true)
    }

    open fun setSectionIcon() {
        action_image.gone()
    }


    open fun setSectionIcon(iconUrl: String) {
        if (iconUrl.isNotEmpty() && iconUrl.isNotBlank()) {
            action_image.visible()
            Glide.with(context)
                .load(iconUrl)
                .into(action_image)
        }
        else setSectionIcon()
    }

    override fun bind(data: Any?) {
        if(data is OtherFeatureItemDVM){
            if(sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                view.findViewById<TextView>(R.id.action_title).text = data.hi?.title?:data.title
            }else{
                view.findViewById<TextView>(R.id.action_title).text = data.title
            }
            setSectionIcon(data.image)
            view.setOnClickListener{
                navigation.navigateTo(data.navPath)
            }
        }

    }
}