package com.gigforce.app.modules.learning.data

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

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive : Boolean = false
)