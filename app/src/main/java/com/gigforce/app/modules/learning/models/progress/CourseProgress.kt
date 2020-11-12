package com.gigforce.app.modules.learning.models.progress

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class CourseProgress(
    var progressId : String = "",

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid : String = "",

    @get:PropertyName("course_id")
    @set:PropertyName("course_id")
    var courseId : String = "",

    @get:PropertyName("completed_modules")
    @set:PropertyName("completed_modules")
    var completedModules : Int = 0,

    @get:PropertyName("total_modules")
    @set:PropertyName("total_modules")
    var totalModules : Int = 0,


    @get:PropertyName("course_start_date")
    @set:PropertyName("course_start_date")
    var courseStartDate : Timestamp? = null,

    @get:PropertyName("course_completion_date")
    @set:PropertyName("course_completion_date")
    var courseCompletionDate : Timestamp? = null,

    @get:PropertyName("ongoing")
    @set:PropertyName("ongoing")
    var ongoing : Boolean = false,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed : Boolean = false,

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type : String = "course"
)