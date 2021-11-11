package com.gigforce.core.datamodels.learning

import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class LessonProgress(
    var progressTrackingId: String = "",

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = "",

    @get:PropertyName("course_id")
    @set:PropertyName("course_id")
    var courseId: String = "",

    @get:PropertyName("module_id")
    @set:PropertyName("module_id")
    var moduleId: String = "",

    @get:PropertyName("lesson_id")
    @set:PropertyName("lesson_id")
    var lessonId: String = "",

    @get:PropertyName("lesson_start_date")
    @set:PropertyName("lesson_start_date")
    var lessonStartDate: Timestamp? = null,

    @get:PropertyName("lesson_completion_date")
    @set:PropertyName("lesson_completion_date")
    var lessonCompletionDate: Timestamp? = null,

    @get:PropertyName("ongoing")
    @set:PropertyName("ongoing")
    var ongoing: Boolean = false,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "lesson",

    @get:PropertyName("priority")
    @set:PropertyName("priority")
    var priority: Int = -1,

    /**
     * Completion progress
     * for Video - seconds played
     * for assessment - 0-pending, 100-done
     * for slides = no of slides covered
     */
    @get:PropertyName("completion_progress")
    @set:PropertyName("completion_progress")
    var completionProgress: Long = 0,

    /**
     * Total length of the lesson
     * for Video - length in seconds
     * for assessment - ignore
     * for slides - no of slides
     */
    @get:PropertyName("lesson_total_length")
    @set:PropertyName("lesson_total_length")
    var lessonTotalLength: Long = 0,

    @get:PropertyName("lesson_type")
    @set:PropertyName("lesson_type")
    var lessonType: String = CourseContent.TYPE_VIDEO,

    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt : Timestamp ?= Timestamp.now(),

    @get:PropertyName("updatedBy")
    @set:PropertyName("updatedBy")
    var updatedBy : String ?= StringConstants.APP.value,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt : Timestamp ?= Timestamp.now()

){
    fun setUpdatedAtAndBy(){
        updatedAt = Timestamp.now()
        updatedBy = StringConstants.APP.value
    }

    fun setCreatedAt(){
        createdAt = Timestamp.now()
    }
}