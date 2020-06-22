package com.gigforce.app.modules.learning.learningVideo

import androidx.annotation.DrawableRes

data class LearningVideo(
    @DrawableRes val thumbnail: Int,
    val title: String,
    val videoLength: String,
    val lessonName: String
)