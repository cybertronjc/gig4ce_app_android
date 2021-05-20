package com.gigforce.app.modules.landingscreen.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.help.HelpVideo
import com.gigforce.app.modules.landingscreen.LandingScreenFragment
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.utils.GlideApp

class HelpVideosAdapter(
    private val context: Context
) : RecyclerView.Adapter<HelpVideosAdapter.HelpVideosViewHolder>(){

    private var originalList: List<HelpVideo> = emptyList()
    private var onCardSelectedListener : OnCardSelectedListener? = null

    fun setOnCardSelectedListener(onCardSelectedListener: OnCardSelectedListener){
        this.onCardSelectedListener = onCardSelectedListener
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpVideosViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.item_help_video, parent, false)
        return HelpVideosViewHolder(view)
    }


    override fun getItemCount(): Int {
        return originalList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == originalList.size - 1 ){
            return 2
        }
        else {
            return 1

        }
    }

    override fun onBindViewHolder(holder: HelpVideosViewHolder, position: Int) {
        holder.bindValues(originalList.get(position), position)
    }

    fun setData(contacts: List<HelpVideo>) {
        this.originalList = contacts
        notifyDataSetChanged()
    }


    inner class HelpVideosViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var title: TextView = itemView.findViewById(R.id.titleTV)
        private var icon: ImageView = itemView.findViewById(R.id.help_first_card_img)
        private var time: TextView = itemView.findViewById(R.id.time_text)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(model: HelpVideo, position: Int) {
            title.text = model.videoTitle
            GlideApp.with(context)
                .load(model!!.getThumbNailUrl()!!)
                .placeholder(getCircularProgressDrawable(context))
                .into(icon)
            time.text = if (model!!.videoLength >= 60) {
                val minutes = model!!.videoLength / 60
                val secs = model!!.videoLength % 60
                "$minutes:$secs"
            } else {
                "00:${model.videoLength}"
            }
        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            val jobProfile = originalList[newPosition]
            onCardSelectedListener?.onCardSelected(jobProfile)
        }

    }


    interface OnCardSelectedListener {

        fun onCardSelected(
            model: HelpVideo
        )
    }


}