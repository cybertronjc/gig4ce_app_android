package com.gigforce.common_ui.viewdatamodels.models

import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class LessonFeedback (

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id : String = "",

    @get:PropertyName("lessonId")
    @set:PropertyName("lessonId")
    var lessonId : String = "",

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid : String = "",

    @get:PropertyName("lessonRating")
    @set:PropertyName("lessonRating")
    var lessonRating : Float? = null,

    @get:PropertyName("explanation")
    @set:PropertyName("explanation")
    var explanation : Boolean? = null,

    @get:PropertyName("completeness")
    @set:PropertyName("completeness")
    var completeness : Boolean? = null,

    @get:PropertyName("easyToUnderStand")
    @set:PropertyName("easyToUnderStand")
    var easyToUnderStand : Boolean? = null,

    @get:PropertyName("videoQuality")
    @set:PropertyName("videoQuality")
    var videoQuality : Boolean? = null,

    @get:PropertyName("soundQuality")
    @set:PropertyName("soundQuality")
    var soundQuality : Boolean? = null,

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt : Timestamp?= Timestamp.now(),

    @get:PropertyName("updatedBy")
    @set:PropertyName("updatedBy")
    var updatedBy : String ?= StringConstants.APP.value,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt : Timestamp?= Timestamp.now()
){
    fun setUpdatedOnAndBy(){
        updatedAt = Timestamp.now()
        updatedBy = StringConstants.APP.value
    }

    fun setCreatedOn(){
        createdAt = Timestamp.now()
    }
}