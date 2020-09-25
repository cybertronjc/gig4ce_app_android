package com.gigforce.app.modules.learning.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Module(

    var id: String = "",

    @get:PropertyName("course_id")
    @set:PropertyName("course_id")
    var courseId: String = "",

    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var title: String = "",

    @get:PropertyName("cover_pic")
    @set:PropertyName("cover_pic")
    var coverPicture: String? = null,

    @get:PropertyName("module_no")
    @set:PropertyName("module_no")
    var moduleNo: Int = 0,

    @get:PropertyName("total_lessons")
    @set:PropertyName("total_lessons")
    var totalLessons: Int = 0,
    var lessonsCompleted: Int = 0,
    var totalAssessments: Int = 0,

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = false,


    @get:PropertyName("module_start_date")
    @set:PropertyName("module_start_date")
    var moduleStartDate : Timestamp? = null,

    @get:PropertyName("module_completion_date")
    @set:PropertyName("module_completion_date")
    var moduleCompletionDate : Timestamp? = null,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed : Boolean = false,

    @get:PropertyName("ongoing")
    @set:PropertyName("ongoing")
    var ongoing : Boolean = false
)