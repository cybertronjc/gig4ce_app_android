package com.gigforce.app.modules.learning.data

import com.google.firebase.firestore.PropertyName

data class CourseContent(

        var id: String = "",

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

        @get:PropertyName("lesson_no")
    @set:PropertyName("lesson_no")
    var lessonNo: Int = 1,

        @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = false,

        @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,

        @get:PropertyName("slides_count")
    @set:PropertyName("slides_count")
    var slidesCount: Int = 5,

        @get:PropertyName("video_length")
    @set:PropertyName("video_length")
    var videoLength: String = "00:03"
) {

    companion object {

        const val TYPE_SLIDE = "slides"
        const val TYPE_VIDEO = "video"
        const val TYPE_ASSESSMENT = "assessment"
    }

}
