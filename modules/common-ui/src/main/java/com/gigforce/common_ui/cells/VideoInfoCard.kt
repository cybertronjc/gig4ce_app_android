package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.VideoInfoDVM
import com.gigforce.core.IViewHolder

class VideoInfoCard (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    private val title : TextView
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_video_info_card, this, true)
        title = this.findViewById(R.id.title)
    }

    override fun bind(data: Any?) {
        if(data is VideoInfoDVM){
            title.text = data.title
        }
    }
}