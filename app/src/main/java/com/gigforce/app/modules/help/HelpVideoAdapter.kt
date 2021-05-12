package com.gigforce.app.modules.help

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.common_ui.utils.getCircularProgressDrawable

class HelpVideoAdapter(private val context: Context) :RecyclerView.Adapter<HelpVideoAdapter.HelpVideoViewHolder>() {

    private var arrHelpVideos = ArrayList<HelpVideo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpVideoViewHolder {
        return HelpVideoViewHolder(LayoutInflater.from(
                parent.context
        ).inflate(R.layout.item_help_video, parent, false))
    }

    inner class HelpVideoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        private var helpFirstCardImg = itemView.findViewById<ImageView>(R.id.help_first_card_img)
        private var titleTextView = itemView.findViewById<TextView>(R.id.titleTV)
        private var timeTextView = itemView.findViewById<TextView>(R.id.time_text)

        fun bindValues(helpVideo: HelpVideo) {
            Glide.with(context).load(helpVideo?.getThumbNailUrl()).placeholder(getCircularProgressDrawable(context)).into(helpFirstCardImg)
            titleTextView.text = helpVideo.videoTitle
            timeTextView.text = if (helpVideo.videoLength >= 60) {
                val minutes = helpVideo.videoLength / 60
                val secs = helpVideo.videoLength % 60
                "$minutes:$secs"
            } else {
                "00:${helpVideo.videoLength}"
            }

            itemView.setOnClickListener{
                val id = arrHelpVideos.get(adapterPosition).videoYoutubeId
                val appIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
                val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=$id")
                )
                try {
                    context.startActivity(appIntent)
                } catch (ex: ActivityNotFoundException) {
                    context.startActivity(webIntent)
                }

            }
        }

    }

    fun setData(arrHelpVideos : ArrayList<HelpVideo>){
        this.arrHelpVideos = arrHelpVideos
    }
    override fun getItemCount() = arrHelpVideos.size

    override fun onBindViewHolder(holder: HelpVideoViewHolder, position: Int) { holder.bindValues(arrHelpVideos.get(position))}

}