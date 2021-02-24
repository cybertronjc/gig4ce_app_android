package com.gigforce.learning.learning.models

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
    var videoUrl: String = "",

    @get:PropertyName("isopened")
    @set:PropertyName("isopened")
    var isOpened : Boolean = false,

    @get:PropertyName("roles_required")
    @set:PropertyName("roles_required")
    var rolesRequired : Boolean = false,

    @get:PropertyName("roles")
    @set:PropertyName("roles")
    var roles : List<String> = emptyList(),

    @get:PropertyName("user_ids_required")
    @set:PropertyName("user_ids_required")
    var userIdRequired : Boolean = false,

    @get:PropertyName("user_uids")
    @set:PropertyName("user_uids")
    var userUids : List<String> = emptyList(),

    @get:PropertyName("clients_required")
    @set:PropertyName("clients_required")
    var clientsRequired : Boolean = false,

    @get:PropertyName("clients")
    @set:PropertyName("clients")
    var clients : List<String> = emptyList()
    ) {

    companion object {

        const val TYPE_SLIDE = "slides"
        const val TYPE_VIDEO = "video"
        const val TYPE_ASSESSMENT = "assessment"
    }

}
