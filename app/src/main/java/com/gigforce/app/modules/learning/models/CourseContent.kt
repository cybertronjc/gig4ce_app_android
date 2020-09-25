package com.gigforce.app.modules.learning.models

import com.google.firebase.firestore.PropertyName

data class CourseContent(

    var id: String = "",

    var progressTrackingId: String = "",

    @get:PropertyName("course_id")
    @set:PropertyName("course_id")
    var courseId: String = "",

    @get:PropertyName("module_id")
    @set:PropertyName("module_id")
    var moduleId: String = "",

    @get:PropertyName("lesson_type")
    @set:PropertyName("lesson_type")
    var type: String = TYPE_VIDEO,

    @get:PropertyName("cover_pic")
    @set:PropertyName("cover_pic")
    var coverPicture: String? = null,

    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var title: String = "",

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = false,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,

    @get:PropertyName("slides_count")
    @set:PropertyName("slides_count")
    var slidesCount: Int = 0,

    @get:PropertyName("priority")
    @set:PropertyName("priority")
    var priority: Int = 0,

    @get:PropertyName("video_length")
    @set:PropertyName("video_length")
    var videoLengthString: String = "00:00",

    @get:PropertyName("url")
    @set:PropertyName("url")
    var videoUrl: String = "",

    /**
     * Completion progress
     * for Video - seconds played
     * for assessment - 0-pending, 100-done
     * for slides = no of slides covered
     */
    var completionProgress : Long = 0,

    /**
     * Total length of the lesson
     * for Video - length in seconds
     * for assessment - ignore
     * for slides - no of slides
     */
    var lessonTotalLength : Long = 0,

    /**
     * Currently Ongoing lesson
     */
    var currentlyOnGoing: Boolean = false

    ) {

    companion object {

        const val TYPE_SLIDE = "slides"
        const val TYPE_VIDEO = "video"
        const val TYPE_ASSESSMENT = "assessment"
    }
}
