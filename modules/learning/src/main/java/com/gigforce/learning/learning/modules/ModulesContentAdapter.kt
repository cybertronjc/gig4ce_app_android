package com.gigforce.learning.learning.modules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.learning.R
//import com.gigforce.app.R
import com.gigforce.core.datamodels.learning.CourseContent
import com.gigforce.common_ui.utils.VectorDrawableUtils
import kotlinx.android.synthetic.main.fragment_learning_video_item.view.*

class ModulesContentAdapter(
    private val mCourseContent: List<CourseContent>
) : RecyclerView.Adapter<ModulesContentAdapter.TimeLineViewHolder>() {

    private var learningVideoActionListener: ((Int) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater

    fun setOnLearningVideoActionListener(listener : (Int) -> Unit){
        this.learningVideoActionListener = listener
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
        holder.videoTitle.text = videoModel.title
//        holder.videoTimeTV.text = videoModel.videoLength
//        holder.lessonNameTV.text = videoModel.lessonName
//        holder.lessonsSeeMoreButton.text = videoModel.lessonsSeeMoreButton
//        Glide.with(holder.videoThumbnailIV.context).load(videoModel.thumbnail)
//            .into(holder.videoThumbnailIV)
    }



    override fun getItemCount() = mCourseContent.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val videoThumbnailIV = itemView.videoThumbnailIV
        val videoTitle = itemView.video_title
        val lessonsSeeMoreButton = itemView.lessonsSeeMoreButton
        val videoTimeTV = itemView.video_time

        init {
            lessonsSeeMoreButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            learningVideoActionListener?.invoke(adapterPosition)
        }
    }

}