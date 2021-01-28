package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.VideoInfoLayoutDVM
import com.gigforce.core.IViewHolder
import kotlinx.android.synthetic.main.cell_video_info_card.view.*

class VideoInfoLayoutComponent (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_video_info_card, this, true)

    }

    override fun bind(data: Any?) {
        if(data is VideoInfoLayoutDVM){
            if(data.image is String && data.image.contains("http")){
                Glide.with(context)
                    .load(data.image)
                    .into(image)
            }
            else if(data.image is Int){
                image.setImageResource(data.image)
            }
            else{
                //layout issue if iv is gone
//                img.gone()
            }

            title.text = data.title
            videos_list.collection = data.allVideos
            load_more.text = data.loadMore
        }
    }
}