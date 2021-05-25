package com.gigforce.app.modules.userLocationCapture.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class UserLocation(
        val location : GeoPoint,
        val fakeLocation : Boolean,
        val locationAccuracy : Float,
        val fullAddressFromGps : String,
        val locationCapturedTime : Timestamp,
        val uid : String,
        val userName : String?,
        val userPhoneNumber : String?,
        val gigId : String?
)
