package com.gigforce.app.modules.learning.models.progress

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ModuleProgress(
    var progressId : String = "",

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid : String = "",

    @get:PropertyName("course_id")
    @set:PropertyName("course_id")
    var courseId : String = "",

    @get:PropertyName("module_id")
    @set:PropertyName("module_id")
    var moduleId : String = "",

    @get:PropertyName("lessons_completed")
    @set:PropertyName("lessons_completed")
    var lessonsCompleted : Int = 0,

    @get:PropertyName("module_start_date")
    @set:PropertyName("module_start_date")
    var moduleStartDate : Timestamp? = null,

    @get:PropertyName("module_completion_date")
    @set:PropertyName("module_completion_date")
    var moduleCompletionDate : Timestamp? = null,

    @get:PropertyName("ongoing")
    @set:PropertyName("ongoing")
    var ongoing : Boolean = false,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed : Boolean = false,

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type : String = "module",

    @get:PropertyName("lesson_progress")
    @set:PropertyName("lesson_progress")
    var lessonsProgress : List<LessonProgress> = emptyList(),

    @get:PropertyName("slide_progress")
    @set:PropertyName("slide_progress")
    var slidesProgress : List<SlideProgress> = emptyList()


    )