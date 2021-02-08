package com.gigforce.common_ui.molecules

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.VideoItemCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

class VideoItemCardComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    var timeText: TextView
    var title: TextView
    var mainImg: ImageView
    @Inject
    lateinit var navigation : INavigation
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
            timeText.text = data.getVideoLength()
//            if (data.image is String) {
//                if(data.image.contains("http")) {
            Glide.with(context)
                .load(data.thumbnail)
                .into(mainImg)
            this.setOnClickListener {
                data.type?.let {
                    when (it) {
                        "youtube_video" -> playvideo(data.link)
                        "navigation" -> navigation.navigateTo(data.navPath?:"")
                        else -> Log.e(
                            "click",
                            "not found"
                        )//Toast.makeText(context,"No Action found",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun playvideo(link: String?) {
        val appIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(link))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(link)
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }
}
