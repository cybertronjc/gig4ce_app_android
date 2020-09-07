package com.gigforce.app.modules.learning.models

import com.google.firebase.firestore.PropertyName

data class SlideContentRemote(

    var id: String = "",

    @get:PropertyName("lesson_id")
    @set:PropertyName("lesson_id")
    var lessonId: String = "",

    @get:PropertyName("course_id")
    @set:PropertyName("course_id")
    var courseId: String = "",

    @get:PropertyName("module_id")
    @set:PropertyName("module_id")
    var moduleId: String = "",

    @get:PropertyName("topictype")
    @set:PropertyName("topictype")
    var type: String = TYPE_VIDEO,

    @get:PropertyName("cover_pic")
    @set:PropertyName("cover_pic")
    var coverPicture: String? = null,

    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var title: String = "",

    @get:PropertyName("slide_no")
    @set:PropertyName("slide_no")
    var slideNo: Int = 0,

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = false,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,

    @get:PropertyName("url")
    @set:PropertyName("url")
    var videoUrl: String = ""
    ) {

    companion object {

        const val TYPE_SLIDE = "slides"
        const val TYPE_VIDEO = "video"
        const val TYPE_ASSESSMENT = "assessment"
    }

}
