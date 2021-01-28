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
import com.gigforce.common_ui.viewdatamodels.VideoItemCardDVM
import com.gigforce.core.IViewHolder

class VideoItemCardComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    var timeText: TextView
    var title: TextView
    var mainImg: ImageView

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_video_item_card, this, true)
        timeText = findViewById(R.id.time_text)
        title = findViewById(R.id.title)
        mainImg = findViewById(R.id.main_img)

    }

    override fun bind(data: Any?) {
        if (data is VideoItemCardDVM) {
            title.text = data.title
            timeText.text = data.time
            if (data.image is String) {
                if(data.image.contains("http")) {
                    Glide.with(context)
                        .load(data.image)
                        .into(mainImg)
                }
            } else if (data.image is Int) {
                mainImg.setImageResource(data.image as Int)
            } else {
            }

        }
    }
}