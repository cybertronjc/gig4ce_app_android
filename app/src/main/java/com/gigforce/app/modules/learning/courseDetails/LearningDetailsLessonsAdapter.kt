package com.gigforce.app.modules.learning.courseDetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.modules.CourseContent
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.VectorDrawableUtils
import com.github.vipulasri.timelineview.TimelineView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_learning_video_item.view.*

class LearningDetailsLessonsAdapter constructor(
    private val context: Context
) :
    RecyclerView.Adapter<LearningDetailsLessonsAdapter.TimeLineViewHolder>() {

    private var learningVideoActionListener: ((Int) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater
    private var mCourseContent: List<CourseContent> = emptyList()

    fun setOnLearningVideoActionListener(listener: (Int) -> Unit) {
        this.learningVideoActionListener = listener
    }

    fun updateCourseContent(courseContent: List<CourseContent>) {
        this.mCourseContent = courseContent
        notifyDataSetChanged()
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

        if (videoModel.completed)
            setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)
        else
            setMarker(holder, R.drawable.ic_marker_inactive, R.color.colorPrimary)



        if (videoModel.type == CourseContent.TYPE_ASSESSMENT) {
            holder.videoSlideLayout.gone()
            holder.assessmentLayout.visible()

            holder.assessmentTitle.text = videoModel.title
            holder.assessmentEstTime.text = videoModel.videoLength

            if (videoModel.completed) {
                holder.assessmentStatus.text = "COMPLETED"
                holder.assessmentStatus.setBackgroundResource(R.drawable.rect_assessment_status_completed)
                holder.asessmentSideStausStrip.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.status_bg_completed,
                        null
                    )
                )
            } else {
                holder.assessmentStatus.text = "PENDING"
                holder.assessmentStatus.setBackgroundResource(R.drawable.rect_assessment_status_pending)
                holder.asessmentSideStausStrip.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.status_bg_pending,
                        null
                    )
                )
            }

        } else {
            holder.assessmentLayout.gone()
            holder.videoSlideLayout.visible()

            if (!videoModel.coverPicture.isNullOrBlank()) {
                if (videoModel.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                        .load(videoModel.coverPicture)
                        .placeholder(getCircularProgressDrawable())
                        .into(holder.slideVideoThumbnail)
                } else {
                    FirebaseStorage.getInstance()
                        .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                        .child(videoModel.coverPicture!!)
                        .downloadUrl
                        .addOnSuccessListener { fileUri ->

                            GlideApp.with(context)
                                .load(fileUri)
                                .placeholder(getCircularProgressDrawable())
                                .into(holder.slideVideoThumbnail)
                        }
                }
            } else {
                holder.slideVideoThumbnail.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.warm_grey,
                        null
                    ))
            }

            holder.videoTitle.text = videoModel.title
            holder.slideVideoLessonNoTV.text = "Lesson ${videoModel.lessonNo}"

            if (videoModel.type == CourseContent.TYPE_SLIDE) {
                holder.slideVideoTimeImageLabel.gone()
                holder.slideVideoLength.text = "${videoModel.slidesCount} Slides"
            } else {
                holder.slideVideoTimeImageLabel.visible()
                holder.slideVideoLength.text = videoModel.videoLength
            }
        }
    }

    private fun setMarker(holder: TimeLineViewHolder, drawableResId: Int, colorFilter: Int) {
        holder.timeline.marker = VectorDrawableUtils.getDrawable(
            holder.itemView.context,
            drawableResId,
            ContextCompat.getColor(holder.itemView.context, colorFilter)
        )
    }

    override fun getItemCount() = mCourseContent.size

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val videoSlideLayout = itemView.course_content_video_slide_layout
        val assessmentLayout = itemView.course_content_assessment_layout

        //Slide Layouts

        val slideVideoThumbnail = itemView.videoThumbnailIV
        val slideVideoTitle = itemView.video_title
        val slideVideoLength = itemView.video_time
        val slideVideoTimeImageLabel = itemView.time_imageview
        val slideVideoLessonNoTV = itemView.lessonNameTV
        val slideVideoplayButton = itemView.time_imageview

        //Assessment
        val assessmentTitle = itemView.title
        val assessmentStatus = itemView.status
        val asessmentSideStausStrip = itemView.side_bar_status
        val assessmentEstTime = itemView.time

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
