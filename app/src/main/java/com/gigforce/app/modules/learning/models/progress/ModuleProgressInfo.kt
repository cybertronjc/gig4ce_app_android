package com.gigforce.app.modules.learning.models.progress

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ModuleProgressInfo (
    @get:PropertyName("startedDate")
    @set:PropertyName("startedDate")
    var startedDate : Timestamp = Timestamp.now(),

    @get:PropertyName("completionDate")
    @set:PropertyName("completionDate")
    var completionDate : Timestamp? = null
)