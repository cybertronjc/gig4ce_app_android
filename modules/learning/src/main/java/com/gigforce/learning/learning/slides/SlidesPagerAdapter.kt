package com.gigforce.learning.learning.slides

import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.gigforce.learning.learning.models.SlideContent
import com.gigforce.learning.learning.slides.types.*

class SlidesPagerAdapter constructor(
    fm: FragmentManager,
    private val moduleId : String,
    private val lessonId : String,
    private val slideList: List<SlideContent>,
    private val videoFragmentOrientationListener: VideoFragmentOrientationListener
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        if (position == slideList.size)
            return SlidesCompletedFragment.getInstance(moduleId, lessonId)

        val slide = slideList[position]
        when (slide.type) {
            SlideContent.TYPE_ASSESSMENT -> {

                val assessmentId = slide.assessmentId
                    ?: throw IllegalStateException("assessment id not for slide type assessment")
                val assessmentTitle = slide.title
                val assessmentDesc = slide.description

                return AssessmentSlideFragment.getInstance(
                    assessmentId = assessmentId,
                    assessmentTitle = assessmentTitle,
                    assessmentDescription = assessmentDesc
                )
            }
            SlideContent.TYPE_BULLET_POINT -> {
                throw IllegalArgumentException("Illegal Slide type : ${slide.type}")
            }
            SlideContent.TYPE_DOS_DONTS -> {

                val lessonId = slide.lessonId
                val doImage = slide.doImage?.toUri()
                    ?: throw IllegalArgumentException(" no do image provided for lesson : $lessonId")
                val doText = slide.doText ?: "Description not provided"
                val dontImage = slide.dontImage?.toUri()
                    ?: throw IllegalArgumentException(" no do image provided for lesson : $lessonId")
                val dontText = slide.dontText ?: "Description not provided"

                return DoAndDontImageFragment.getInstance(
                    lessonId = lessonId,
                    doImageUri = doImage,
                    doImageText = doText,
                    dontImageUri = dontImage,
                    dontImageText = dontText
                )
            }
            SlideContent.TYPE_IMAGE_WITH_TEXT -> {

                val lessonId = slide.lessonId
                val imageUri = slide.image?.toUri()
                    ?: throw IllegalArgumentException(" no image image provided for lesson : $lessonId")
                val title = slide.title
                val description = slide.description

                return SingleImageFragment.getInstance(
                    lessonId = lessonId,
                    imageUri = imageUri,
                    title = title,
                    description = description
                )
            }
            SlideContent.TYPE_VIDEO_WITH_TEXT -> {

                val lessonId = slide.lessonId
                val slideId = slide.slideId
                val videoUri = slide.videoPath?.toUri()
                    ?: throw IllegalArgumentException(" no image image provided for lesson : $lessonId")
                val title = slide.title
                val description = slide.description

                return VideoWithTextFragment.getInstance(
                    lessonId = lessonId,
                    slideId = slideId,
                    videoUri = videoUri,
                    title = title,
                    description = description,
                    baseFragOrientationListener = videoFragmentOrientationListener
                )
            }
            else -> {
                throw IllegalArgumentException("Illegal Slide type : ${slide.type}")
            }
        }
    }

    override fun getCount(): Int = slideList.size + 1

    /**
     *
     */
    fun dispatchOnBackPressedIfCurrentFragmentIsVideoFragment(currentPosition: Int): Boolean {
        val fragment = getItem(currentPosition)

        return if (fragment is VideoWithTextFragment) {
            fragment.backButtonPressed()
        } else false
    }
}