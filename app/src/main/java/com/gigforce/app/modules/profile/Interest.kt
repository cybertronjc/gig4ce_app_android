package com.gigforce.app.modules.profile

import com.google.firebase.firestore.PropertyName

data class Interest (

    @get:PropertyName("id")
    @set:PropertyName("id")
    var name : String = "",

    @get:PropertyName("haveExperience")
    @set:PropertyName("haveExperience")
    var haveExperience : Boolean = false
)