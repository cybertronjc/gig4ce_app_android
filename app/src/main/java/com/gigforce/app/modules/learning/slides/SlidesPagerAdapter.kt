package com.gigforce.app.modules.learning.slides

import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.gigforce.app.modules.learning.data.SlideContent
import com.gigforce.app.modules.learning.slides.types.AssessmentSlideFragment
import com.gigforce.app.modules.learning.slides.types.DoAndDontImageFragment
import com.gigforce.app.modules.learning.slides.types.SingleImageFragment
import com.gigforce.app.modules.learning.slides.types.VideoWithTextFragment

class SlidesPagerAdapter constructor(
    fm: FragmentManager,
    private val slideList: List<SlideContent>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val slide = slideList[position]

       return when (slide.type) {
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
                    description = description
                )
            }
            else -> {
                throw IllegalArgumentException("Illegal Slide type : ${slide.type}")
            }
        }
    }

    override fun getCount(): Int = slideList.size
}