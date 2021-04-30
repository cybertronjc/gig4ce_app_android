package com.gigforce.profile.datamodel

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class EnrollmentInfo(

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("enrolledOn")
    @set:PropertyName("enrolledOn")
    var enrolledOn: Timestamp? = null,

    @get:PropertyName("enrolledLocationLatitude")
    @set:PropertyName("enrolledLocationLatitude")
    var enrolledLocationLatitude: Double = 0.0,

    @get:PropertyName("enrolledLocationLongitude")
    @set:PropertyName("enrolledLocationLongitude")
    var enrolledLocationLongitude: Double = 0.0,

    @get:PropertyName("enrolledLocationAddress")
    @set:PropertyName("enrolledLocationAddress")
    var enrolledLocationAddress: String = ""

){}