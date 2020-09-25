package com.gigforce.app.modules.learning.models.progress

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class SlideProgress(
    var progressId: String = "",

    @get:PropertyName("slide_id")
    @set:PropertyName("slide_id")
    var slideId: String = "",

    @get:PropertyName("slide_start_date")
    @set:PropertyName("slide_start_date")
    var slideStartDate: Timestamp? = null,

    @get:PropertyName("slide_completion_date")
    @set:PropertyName("slide_completion_date")
    var slideCompletionDate: Timestamp? = null,

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,

    @get:PropertyName("total_length")
    @set:PropertyName("total_length")
    var totalLength: Long = 0,

    @get:PropertyName("completion_progress")
    @set:PropertyName("completion_progress")
    var completionProgress: Long = 0
    )