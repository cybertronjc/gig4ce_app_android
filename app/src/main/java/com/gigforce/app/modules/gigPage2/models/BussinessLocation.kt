package com.gigforce.app.modules.gigPage2.models

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import java.io.Serializable
import java.util.*

data class BussinessLocation (

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String? = null,

    @get:PropertyName("geoPoint")
    @set:PropertyName("geoPoint")
    var geoPoint: GeoPoint? = null


)
