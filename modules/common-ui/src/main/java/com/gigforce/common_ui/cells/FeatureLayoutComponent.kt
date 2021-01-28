package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import kotlinx.android.synthetic.main.feature_layout.view.*

class FeatureLayoutComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.feature_layout, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.FeatureLayoutComponent, 0, 0)
            val orientationValue =
                styledAttributeSet.getInt(R.styleable.FeatureLayoutComponent_android_orientation, 0)
            val noOfRows =
                styledAttributeSet.getInt(R.styleable.FeatureLayoutComponent_rows, 1)
            setOrientationAndRows(orientationValue, noOfRows)
        }
    }

    fun setOrientationAndRows(orientation: Int, noOfRows: Int) {
        featured_rv.setOrientationAndRows(orientation, noOfRows)
    }

    override fun bind(data: Any?) {
        if (data is FeatureLayoutDVM) {
            layout_title.text = data.title
            if (data.image is Int) {
                image.setImageResource(data.image)
            } else if (data.image is String) {
                if (data.image.contains("http")) {
                    Glide.with(context)
                        .load(data.image)
                        .into(image)
                } else {
//                    imgLayout.gone()
                }
            } else {
                layout_img.gone()
            }
            featured_rv.collection = data.featuredData
        }
    }
}