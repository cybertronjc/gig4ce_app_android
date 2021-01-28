package com.gigforce.common_ui.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone

class FeatureItemCardComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    val title: TextView
    val subtitle: TextView
    val image: ImageView

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.feature_item_card, this, true)
        title = this.findViewById(R.id.title)
        subtitle = this.findViewById(R.id.subtitle)
        image = this.findViewById(R.id.imgage)
    }

    override fun bind(data: Any?) {
        if (data is FeatureItemCardDVM) {
            if (data.image is String) {
                if(data.image.contains("http")) {
                    Glide.with(context)
                        .load(data.image)
                        .into(image)
                }
            } else if (data.image is Int) {
                image.setImageResource(data.image)
            } else {

            }
            if (data.title.isNotBlank())
                title.text = data.title
            else title.gone()
            if (data.subtitle.isNotBlank())
                subtitle.text = data.subtitle
            else subtitle.gone()
        }
    }

}