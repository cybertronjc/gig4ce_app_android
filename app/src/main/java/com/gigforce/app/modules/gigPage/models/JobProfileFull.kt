package com.gigforce.app.modules.gigPage.models

import com.google.firebase.firestore.PropertyName

data class JobProfileFull(

        @get:PropertyName("coverImg")
        @set:PropertyName("coverImg")
        var coverImg: String = "",

        @get:PropertyName("illustration")
        @set:PropertyName("illustration")
        var illustrationImage: String = "",

        @get:PropertyName("info")
        @set:PropertyName("info")
        var info: List<PointsDataWrapper> = listOf()
)


data class PointsDataWrapper(
        @get:PropertyName("pointsData")
        @set:PropertyName("pointsData")
        var pointsData: List<String> = listOf(),

        @get:PropertyName("title")
        @set:PropertyName("title")
        var title: String = ""
)