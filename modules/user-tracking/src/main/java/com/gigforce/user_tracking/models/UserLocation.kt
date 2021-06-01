package com.gigforce.user_tracking.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class UserGigLocationTrack(

        @get:PropertyName("uid")
        val uid : String,

        @get:PropertyName("userName")
        val userName : String?,

        @get:PropertyName("userPhoneNumber")
        val userPhoneNumber : String?,

        @get:PropertyName("locations")
        val locations : List<UserLocation>
)

data class UserLocation(

        @get:PropertyName("location")
        val location : GeoPoint,

        @get:PropertyName("fakeLocation")
        val fakeLocation : Boolean,

        @get:PropertyName("locationAccuracy")
        val locationAccuracy : Float,

        @get:PropertyName("fullAddressFromGps")
        val fullAddressFromGps : String,

        @get:PropertyName("locationCapturedTime")
        val locationCapturedTime : Timestamp
)
