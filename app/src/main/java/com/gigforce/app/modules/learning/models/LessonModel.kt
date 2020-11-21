package com.gigforce.app.modules.learning.models

import com.google.firebase.firestore.PropertyName

data class LessonModel (
    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var name : String = "",
    @get:PropertyName("cover_pic")
    @set:PropertyName("cover_pic")
    var coverPicture : String? = null,
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed : Boolean = false,
    @get:PropertyName("description")
    @set:PropertyName("description")
    var description : String = ""
)