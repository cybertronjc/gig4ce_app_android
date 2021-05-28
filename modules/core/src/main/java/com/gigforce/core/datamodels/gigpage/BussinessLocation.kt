package com.gigforce.core.datamodels.gigpage


import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class BussinessLocation (

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String? = null,

    @get:PropertyName("geoPoint")
    @set:PropertyName("geoPoint")
    var geoPoint: GeoPoint? = null


)
