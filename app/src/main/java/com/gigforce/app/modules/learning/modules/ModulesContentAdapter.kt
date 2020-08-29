package com.gigforce.app.modules.learning.modules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.learning.data.CourseContent
import com.gigforce.app.utils.VectorDrawableUtils
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.android.synthetic.main.fragment_learning_video_item.view.*

class ModulesContentAdapter(
    private val mCourseContent: List<CourseContent>
) : RecyclerView.Adapter<ModulesContentAdapter.TimeLineViewHolder>() {

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

        val videoModel = mCourseContent[position]
        setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)

        holder.videoTitle.text = videoModel.title
//        holder.videoTimeTV.text = videoModel.videoLength
//        holder.lessonNameTV.text = videoModel.lessonName
//        holder.lessonsSeeMoreButton.text = videoModel.lessonsSeeMoreButton
//        Glide.with(holder.videoThumbnailIV.context).load(videoModel.thumbnail)
//            .into(holder.videoThumbnailIV)
    }

    private fun setMarker(holder: TimeLineViewHolder, drawableResId: Int, colorFilter: Int) {
        holder.timeline.marker = VectorDrawableUtils.getDrawable(
            holder.itemView.context,
            drawableResId,
            ContextCompat.getColor(holder.itemView.context, colorFilter)
        )
    }


    override fun getItemCount() = mCourseContent.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val videoThumbnailIV = itemView.videoThumbnailIV
        val videoTitle = itemView.video_title
        val lessonNameTV = itemView.lessonNameTV
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