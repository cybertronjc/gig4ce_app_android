package com.gigforce.app.modules.learning.modules

import com.google.firebase.firestore.PropertyName

data class SlideContent(

    var slideId: String = "",
    var lessonId: String = "",

    @get:PropertyName("slide_no")
    @set:PropertyName("slide_no")
    var slideNo: Int = 1,

    @get:PropertyName("slide_type")
    @set:PropertyName("slide_type")
    var type: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = false,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,



    //Video Slide Excl

    @get:PropertyName("video_path")
    @set:PropertyName("video_path")
    var videoPath : String? = null,


    //Image with text excl

    @get:PropertyName("image")
    @set:PropertyName("image")
    var image: String? = null,


    //Do and dont excl

    @get:PropertyName("do_image")
    @set:PropertyName("do_image")
    var doImage: String? = null,

    @get:PropertyName("do_text")
    @set:PropertyName("do_text")
    var doText: String? = null,

    @get:PropertyName("dont_image")
    @set:PropertyName("dont_image")
    var dontImage: String? = null,

    @get:PropertyName("dont_text")
    @set:PropertyName("dont_text")
    var dontText: String? = null,


    //Assessment Slide

    @get:PropertyName("assessment_id")
    @set:PropertyName("assessment_id")
    var assessmentId: String? = null

) {

    companion object {

        const val TYPE_DOS_DONTS = "dos_and_donts"
        const val TYPE_VIDEO_WITH_TEXT = "video_with_text"
        const val TYPE_IMAGE_WITH_TEXT = "image_with_text"
        const val TYPE_ASSESSMENT = "assessment"
        const val TYPE_BULLET_POINT = "bullet_point_with_text"
    }

}
