package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.recyclerView.CoreRecyclerView
import kotlinx.android.synthetic.main.feature_layout.view.*

open class FeatureLayoutComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    val view : View
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.feature_layout, this, true)
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

    fun getFeatureRV() : CoreRecyclerView{
        return featured_rv
    }

    open fun setSectionTitle(title: String) {
        layout_title.text = title
    }

    open fun setSectionIcon() {
        layout_img.gone()
    }

    open fun setSectionIcon(imageResource: Int) {
        layout_img.visible()
        image.setImageResource(imageResource)
    }

    open fun setSectionIcon(iconUrl: String) {
        layout_img.visible()
        Glide.with(context)
            .load(iconUrl)
            .into(image)
    }

    open fun setSectionIcon(imageData: Any) {
        if (imageData is Int) setSectionIcon(imageData)
        else if (imageData is String && imageData.isNotEmpty() && imageData.isNotBlank()) {
            setSectionIcon(imageData)
        }
        else setSectionIcon()
    }

    open fun setCollection(data: List<Any>) {
        featured_rv.collection = data
    }

    fun enableSeemoreButton() {
        see_more_btn.visible()
    }

    override fun bind(data: Any?) {
        if (data is FeatureLayoutDVM) {
            if (data.title.isNotEmpty() && data.title.isNotBlank()) {
                view.findViewById<ConstraintLayout>(R.id.title_cl).visible()
                this.setSectionTitle(data.title)
            } else {
                view.findViewById<ConstraintLayout>(R.id.title_cl).gone()
            }
            this.setSectionIcon(data.image)
            if(data.collection.size>0) {
                view.findViewById<ConstraintLayout>(R.id.top_cl).visible()
                this.setCollection(data.collection)
            }
            else view.findViewById<ConstraintLayout>(R.id.top_cl).gone()
        }
    }
}