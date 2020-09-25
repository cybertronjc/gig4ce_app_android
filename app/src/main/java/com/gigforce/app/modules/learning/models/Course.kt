package com.gigforce.app.modules.learning.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Course (

    var id : String = "",

    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var name : String = "",

    @get:PropertyName("course_description")
    @set:PropertyName("course_description")
    var description : String? = null,

    @get:PropertyName("module_count")
    @set:PropertyName("module_count")
    var moduleCount : Int = 0,

    @get:PropertyName("cover_pic")
    @set:PropertyName("cover_pic")
    var coverPicture : String? = null,

    @get:PropertyName("priority")
    @set:PropertyName("priority")
    var priority : Int = 0,

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive : Boolean = false,

    @get:PropertyName("course_start_date")
    @set:PropertyName("course_start_date")
    var courseStartDate : Timestamp? = null,

    @get:PropertyName("Level")
    @set:PropertyName("Level")
    var level : String? = null,

    @get:PropertyName("course_completion_date")
    @set:PropertyName("course_completion_date")
    var courseCompletionDate : Timestamp? = null,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed : Boolean = false
)