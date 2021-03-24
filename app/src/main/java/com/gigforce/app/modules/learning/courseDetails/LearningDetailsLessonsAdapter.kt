package com.gigforce.app.modules.learning.courseDetails

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.core.utils.GlideApp
import com.github.vipulasri.timelineview.TimelineView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_learning_video_item.view.*

class LearningDetailsLessonsAdapter constructor(
        private val context: Context
) :
        RecyclerView.Adapter<LearningDetailsLessonsAdapter.TimeLineViewHolder>() {

    private var learningVideoActionListener: ((CourseContent) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater
    private var mCourseContent: List<CourseContent> = emptyList()

    fun setOnLearningVideoActionListener(listener: (CourseContent) -> Unit) {
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
        //  val viewType = getItemViewType(position)

        if (videoModel.type == CourseContent.TYPE_ASSESSMENT) {
            holder.videoSlideLayout.gone()
            holder.assessmentLayout.visible()

            holder.assessmentTitle.text = videoModel.title
            holder.assessmentEstTime.text = videoModel.videoLengthString

            if (videoModel.completed) {
                holder.assessmentStatus.text = "COMPLETED"

//                holder.timeline.setStartLineColor(R.color.colorPrimary, viewType)
//                holder.timeline.setEndLineColor(R.color.colorPrimary, viewType)

                //  setMarker(holder, R.drawable.ic_marker, R.color.colorPrimary)

                holder.assessmentStatus.setBackgroundResource(R.drawable.rect_assessment_status_completed)
                holder.asessmentSideStausStrip.setCardBackgroundColor(
                        ResourcesCompat.getColor(
                                context.resources,
                                R.color.status_bg_completed,
                                null
                        )
                )
            } else {
                //Not even started
//                if (videoModel.currentlyOnGoing) {
//                    setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)
//                } else {
//                    setMarker(holder, R.drawable.ic_marker_inactive, R.color.colorPrimary)
//                }

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
        } else if (videoModel.type == CourseContent.TYPE_SLIDE) {
            holder.assessmentLayout.gone()
            holder.videoSlideLayout.visible()

            if (!videoModel.coverPicture.isNullOrBlank()) {
                if (videoModel.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                            .load(videoModel.coverPicture)
                            .placeholder(getCircularProgressDrawable())
                            .error(R.drawable.ic_learning_default_back)
                            .into(holder.slideVideoThumbnail)
                } else {
                    val imageRef = FirebaseStorage.getInstance()
                            .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                            .child(videoModel.coverPicture!!)

                    GlideApp.with(context)
                            .load(imageRef)
                            .placeholder(getCircularProgressDrawable())
                            .error(R.drawable.ic_learning_default_back)
                            .into(holder.slideVideoThumbnail)
                }
            } else {
                holder.slideVideoThumbnail.setBackgroundColor(
                        ResourcesCompat.getColor(
                                context.resources,
                                R.color.warm_grey,
                                null
                        )
                )
            }

            holder.videoTitle.text = videoModel.title

            holder.slideVideoTimeImageLabel.gone()
            holder.slideVideoLength.text = "${videoModel.slidesCount} Slides"
            holder.slidePlayIV.setImageResource(R.drawable.ic_slides)

            if (videoModel.completed) {
//                setMarker(holder, R.drawable.ic_marker, R.color.colorPrimary)
                holder.lessonCompletionPercentage.text = "Completed 100%"
                holder.lessonsSeeMoreButton.text = "Re-play"
                holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_green, null))
            } else /*if (videoModel.currentlyOnGoing)*/ {
                if (videoModel.completionProgress == 0L) {
                    //Not even started
                    //Not even started
                    //                  setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)
                    holder.lessonCompletionPercentage.text = "Pending 0%"
                    holder.lessonsSeeMoreButton.text = "Play"
                    holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_yellow, null))
                } else {
                    //Currently going on
                    //                setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)

                    val completedPercentage: Long = if (videoModel.lessonTotalLength != 0L)
                        (videoModel.completionProgress * 100) / videoModel.lessonTotalLength
                    else
                        0

                    holder.lessonCompletionPercentage.text = "Completed $completedPercentage%"
                    holder.lessonsSeeMoreButton.text = "Resume"
                    holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_orange, null))
                }
            }
            //else {
            //          setMarker(holder, R.drawable.ic_marker_inactive, R.color.colorPrimary)
            //}


        } else if (videoModel.type == CourseContent.TYPE_VIDEO) {
            holder.assessmentLayout.gone()
            holder.videoSlideLayout.visible()

            if (!videoModel.coverPicture.isNullOrBlank()) {
                if (videoModel.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                            .load(videoModel.coverPicture)
                            .placeholder(getCircularProgressDrawable())
                            .error(R.drawable.ic_learning_default_back)
                            .into(holder.slideVideoThumbnail)
                } else {
                    val imageRef = FirebaseStorage.getInstance()
                            .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                            .child(videoModel.coverPicture!!)

                    GlideApp.with(context)
                            .load(imageRef)
                            .placeholder(getCircularProgressDrawable())
                            .error(R.drawable.ic_learning_default_back)
                            .into(holder.slideVideoThumbnail)

                }
            } else {

                holder.slideVideoThumbnail.setBackgroundColor(
                        ResourcesCompat.getColor(
                                context.resources,
                                R.color.warm_grey,
                                null
                        )
                )
            }

            holder.videoTitle.text = videoModel.title

            holder.videoPlayIV.setImageResource(R.drawable.ic_learning_play)
            holder.slideVideoTimeImageLabel.visible()
            holder.slideVideoLength.text = videoModel.videoLengthString

            if (videoModel.completed) {
                //        setMarker(holder, R.drawable.ic_marker, R.color.colorPrimary)
                holder.lessonCompletionPercentage.text = "Completed 100%"
                holder.lessonsSeeMoreButton.text = "Re-play"
                holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_green, null))
            } else /*if (videoModel.currentlyOnGoing) */ {

                if (videoModel.completionProgress == 0L) {
                    //Not even started
                    //          setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)
                    holder.lessonCompletionPercentage.text = "Completed 0%"
                    holder.lessonsSeeMoreButton.text = "Play"
                    holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_yellow, null))
                } else {
                    //Currently going on
                    //        setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)

                    val completedPercentage: Long = if (videoModel.lessonTotalLength != 0L)
                        (videoModel.completionProgress * 100) / videoModel.lessonTotalLength
                    else
                        0

                    holder.lessonCompletionPercentage.text = "Completed $completedPercentage%"
                    holder.lessonsSeeMoreButton.text = "Resume"
                    holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_orange, null))
                }
            }
            //else {
            //    holder.lessonCompletionPercentage.text = "Pending 0%"
            //  holder.lessonCompletionPercentage.setTextColor(ResourcesCompat.getColor(context.resources,R.color.text_yellow,null))
            //  setMarker(holder, R.drawable.ic_marker_inactive, R.color.colorPrimary)
        }
    }


    private fun setMarker(holder: TimeLineViewHolder, drawableResId: Int, colorFilter: Int) {
//        holder.timeline.marker = VectorDrawableUtils.getDrawable(
//            holder.itemView.context,
//            drawableResId,
//            ContextCompat.getColor(holder.itemView.context, colorFilter)
//        )
    }

    override fun getItemCount() = mCourseContent.size

    fun getCircularProgressDrawable(): Drawable {
        val shimmer = Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
                .setDuration(1800) // how long the shimmering animation takes to do one full sweep
                .setBaseAlpha(0.8f) //the alpha of the underlying children
                .setHighlightAlpha(1.0f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build()


// This is the placeholder for the imageView
        return ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
    }

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val videoSlideLayout = itemView.course_content_video_slide_layout
        val assessmentLayout = itemView.course_content_assessment_layout

        //Slide Layouts

        val slideVideoThumbnail = itemView.videoThumbnailIV
        val slideVideoTitle = itemView.video_title
        val slideVideoLength = itemView.video_time
        val slidePlayIV = itemView.play_button_iv
        val slideVideoTimeImageLabel = itemView.time_imageview
        val slideVideoplayButton = itemView.time_imageview

        //Assessment
        val assessmentRootLayout = itemView.course_content_assessment_layout
        val assessmentTitle = itemView.title
        val assessmentStatus = itemView.status
        val asessmentSideStausStrip = itemView.side_bar_status
        val assessmentEstTime = itemView.time

        val videoThumbnailIV = itemView.videoThumbnailIV
        val videoTitle = itemView.video_title
        val videoPlayIV = itemView.play_button_iv
        val lessonsSeeMoreButton = itemView.lessonsSeeMoreButton
        val videoTimeTV = itemView.video_time
        // val timeline = itemView.timeline


        val lessonCompletionPercentage = itemView.lesson_completion_tv

        init {
            //timeline.initLine(viewType)
            itemView.setOnClickListener(this)
            lessonsSeeMoreButton.setOnClickListener(this)
            assessmentRootLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val content = mCourseContent[adapterPosition]

//            if (content.completed || content.currentlyOnGoing) {
            learningVideoActionListener?.invoke(mCourseContent[adapterPosition])
//            } else {
//                Toast.makeText(
//                    context,
//                    "Please complete previous lessons first",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        }
    }
}
