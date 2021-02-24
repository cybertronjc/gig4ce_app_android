package com.gigforce.learning.learning.learningVideo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learning.R
//import com.gigforce.app.R
import com.gigforce.common_ui.utils.VectorDrawableUtils
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.android.synthetic.main.fragment_learning_video_item.view.*

class LearningVideoLineAdapter(private val mFeedList: List<LearningVideo>) :
    RecyclerView.Adapter<LearningVideoLineAdapter.TimeLineViewHolder>() {

    private var learningVideoActionListener: ((Int) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater

    fun setOnLearningVideoActionListener(listener : (Int) -> Unit){
        this.learningVideoActionListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(
            mLayoutInflater.inflate(
                R.layout.fragment_learning_video_item,
                parent,
                false
            ), viewType
        )
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val videoModel = mFeedList[position]
        setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)

        holder.videoTitle.text = videoModel.title
        holder.videoTimeTV.text = videoModel.videoLength
        holder.lessonsSeeMoreButton.text = videoModel.lessonsSeeMoreButton
        Glide.with(holder.videoThumbnailIV.context).load(videoModel.thumbnail)
            .into(holder.videoThumbnailIV)
    }

    private fun setMarker(holder: TimeLineViewHolder, drawableResId: Int, colorFilter: Int) {
        holder.timeline.marker = VectorDrawableUtils.getDrawable(
            holder.itemView.context,
            drawableResId,
            ContextCompat.getColor(holder.itemView.context, colorFilter)
        )
    }



    override fun getItemCount() = mFeedList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val videoThumbnailIV = itemView.videoThumbnailIV
        val videoTitle = itemView.video_title
        val lessonsSeeMoreButton = itemView.lessonsSeeMoreButton
        val videoTimeTV = itemView.video_time
        val timeline = itemView.timeline

        init {
            timeline.initLine(viewType)
            lessonsSeeMoreButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            learningVideoActionListener?.invoke(adapterPosition)
        }
    }

}