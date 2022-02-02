package com.gigforce.common_ui.chat.models

import com.gigforce.core.SimpleDVM
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class LocationDetailsInfo(

    @get:PropertyName("isLiveLocation")
    @set:PropertyName("isLiveLocation")
    var isLiveLocation: Boolean = false,

    @get:PropertyName("isCurrentlySharingLiveLocation")
    @set:PropertyName("isCurrentlySharingLiveLocation")
    var isCurrentlySharingLiveLocation: Boolean? = false,

    @get:PropertyName("liveEndTime")
    @set:PropertyName("deliveredOn")
    var liveEndTime: Timestamp? = null,

    @get:PropertyName("locationPhysicalAddress")
    @set:PropertyName("locationPhysicalAddress")
    var locationPhysicalAddress: String = "",

)