package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.recyclerView.CoreRecyclerView

class FeatureLayoutComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    val title : TextView
    val imgLayout : ConstraintLayout
    val image : ImageView
    val coreRV : CoreRecyclerView
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.feature_layout, this, true)
        title = this.findViewById(R.id.layout_title)
        imgLayout = this.findViewById(R.id.layout_img)
        image = this.findViewById(R.id.image)
        coreRV = this.findViewById(R.id.featured_rv)
    }

    override fun bind(data: Any?) {
        if(data is FeatureLayoutDVM){
            title.text = data.title
            if(data.image is Int){
                image.setImageResource(data.image)
            }
            else if(data.image is String){
                if(data.image.contains("http")){

                }else{
//                    imgLayout.gone()
                }
            }
            else{
                imgLayout.gone()
            }
            coreRV.collection = data.featuredData
        }
    }
}