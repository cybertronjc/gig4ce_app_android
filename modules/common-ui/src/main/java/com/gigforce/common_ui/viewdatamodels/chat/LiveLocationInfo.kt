package com.gigforce.common_ui.viewdatamodels.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import java.util.*

data class LiveLocationInfo(
    @get:PropertyName("isLiveLocation")
    @set:PropertyName("isLiveLocation")
    var isLiveLocation: Boolean = false,

    @get:PropertyName("isCurrentlySharingLiveLocation")
    @set:PropertyName("isCurrentlySharingLiveLocation")
    var isCurrentlySharingLiveLocation: Boolean = false,

    @get:PropertyName("liveLocation")
    @set:PropertyName("liveLocation")
    var liveLocation: GeoPoint? = null,

    @get:PropertyName("liveEndTime")
    @set:PropertyName("liveEndTime")
    var liveEndTime: Date? = null,


    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt : Timestamp ?= Timestamp.now(),

    @get:PropertyName("updatedBy")
    @set:PropertyName("updatedBy")
    var updatedBy : String ?= null,
    )
