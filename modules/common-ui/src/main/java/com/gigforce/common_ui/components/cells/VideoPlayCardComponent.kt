package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.R
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.VideoPlayCardDVM
import com.gigforce.core.AppConstants
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayCardComponent(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet),
        IViewHolder {

    init {
        this.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.view_play_card_layout, this, true)

    }

    val itemView = this.findViewById<LinearLayout>(R.id.itemView)
    val slideVideoThumbnail = this.findViewById<ImageView>(R.id.videoThumbnailIV)
    val slideVideoLength = this.findViewById<AppCompatTextView>(R.id.video_time)
    val slideVideoTimeImageLabel = this.findViewById<AppCompatImageView>(R.id.time_imageview)
    val videoThumbnailIV = this.findViewById<ImageView>(R.id.videoThumbnailIV)
    val videoTitle = this.findViewById<TextView>(R.id.video_title)
    val videoPlayIV = this.findViewById<ImageView>(R.id.play_button_iv)
    val lessonsSeeMoreButton = this.findViewById<MaterialButton>(R.id.lessonsSeeMoreButton)
    val lessonCompletionPercentage = this.findViewById<AppCompatTextView>(R.id.lesson_completion_tv)

    @Inject
    lateinit var navigation: INavigation

    fun setCoverPicture(data: VideoPlayCardDVM) {
        if (!data.coverPicture.isNullOrBlank()) {
            if (data.coverPicture.startsWith("http", true)) {

                GlideApp.with(context)
                        .load(data.coverPicture)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(slideVideoThumbnail)
            } else {
                val imageRef = FirebaseStorage.getInstance()
                        .getReference(AppConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                        .child(data.coverPicture)

                GlideApp.with(context)
                        .load(imageRef)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(slideVideoThumbnail)

            }
        } else {

            slideVideoThumbnail.setBackgroundColor(
                    ResourcesCompat.getColor(
                            context.resources,
                            R.color.warm_grey,
                            null
                    )
            )
        }
    }

    fun setVideoTitle(title: String) {
        videoTitle.text = title
    }

    fun actionOnCardClick(data: VideoPlayCardDVM) {
        data.fragment?.let { fragment ->
            data.lessonId?.let { lessonId ->
                data.moduleId?.let { moduleId ->
                    data.shouldShowFeedbackDialog?.let { shouldShowFeedbackDialog ->
                        navigation.navigateToPlayVideoDialogFragment(fragment, moduleId, lessonId, shouldShowFeedbackDialog)
                    }
                }
            }
        }
    }

    override fun bind(data: Any?) {
        if (data is VideoPlayCardDVM) {
            itemView.setOnClickListener(null)
            itemView.setOnClickListener {
                actionOnCardClick(data)
            }
            lessonsSeeMoreButton.setOnClickListener(null)
            lessonsSeeMoreButton.setOnClickListener {
                actionOnCardClick(data)
            }
            setCoverPicture(data)
            data.title?.let { setVideoTitle(it) }

            videoPlayIV?.setImageResource(R.drawable.ic_learning_play)
            slideVideoTimeImageLabel?.visible()
            slideVideoLength?.text = data.videoLengthString

            if (data.completed) {
                //        setMarker(holder, R.drawable.ic_marker, R.color.colorPrimary)
                lessonCompletionPercentage?.text = context.getString(R.string.compelted_100_common_ui)
                lessonsSeeMoreButton?.text = context.getString(R.string.replay_common_ui)
                lessonCompletionPercentage?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_green, null))
            } else /*if (data.currentlyOnGoing) */ {

                if (data.completionProgress == 0L) {
                    //Not even started
                    //          setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)
                    lessonCompletionPercentage?.text = context.getString(R.string.completed_0_common_ui)
                    lessonsSeeMoreButton?.text = context.getString(R.string.play_common_ui)
                    lessonCompletionPercentage?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_yellow, null))
                } else {
                    //Currently going on
                    //        setMarker(holder, R.drawable.ic_marker_active, R.color.colorPrimary)

                    val completedPercentage: Long = if (data.lessonTotalLength != 0L)
                        (data.completionProgress * 100) / data.lessonTotalLength
                    else
                        0

                    lessonCompletionPercentage?.text = context.getString(R.string.completed_common_ui) + completedPercentage + "%"
                    lessonsSeeMoreButton?.text = context.getString(R.string.resume_common_ui)
                    lessonCompletionPercentage?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_orange, null))
                }
            }
        }

    }
}