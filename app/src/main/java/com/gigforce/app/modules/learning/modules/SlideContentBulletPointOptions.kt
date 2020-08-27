package com.gigforce.app.modules.learning.modules

import com.google.firebase.firestore.PropertyName

data class SlideContentBulletPointOptions(

    @get:PropertyName("text")
    @set:PropertyName("text")
    var text: String = "",

    @get:PropertyName("image")
    @set:PropertyName("image")
    var image: String? = null
)
