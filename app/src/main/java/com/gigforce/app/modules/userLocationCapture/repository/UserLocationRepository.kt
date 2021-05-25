package com.gigforce.app.modules.userLocationCapture.repository

import android.util.Log
import com.gigforce.app.modules.userLocationCapture.models.UserLocation
import com.gigforce.app.utils.LocationUtils
import com.gigforce.app.utils.addOrThrow
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class UserLocationRepository constructor(
        private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collectionRef: CollectionReference by lazy {
        firebaseFirestore.collection("UserLocations")
    }

    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }

    suspend fun updateUserLocation(
            location: LatLng,
            accuracy : Float,
            gigId :String?,
            userName :String?,
            couldBeAFakeLocation : Boolean,
            fullAddressFromGps : String
    ) {

        if (user == null) {
            Log.d(TAG,"Captured User location but logged in user was null")
            return
        }

        collectionRef.addOrThrow(
            UserLocation(
                    location = GeoPoint(location.latitude,location.longitude),
                    locationAccuracy = accuracy,
                    locationCapturedTime = Timestamp.now(),
                    uid =  user!!.uid,
                    userName = userName,
                    userPhoneNumber = user!!.phoneNumber,
                    gigId = gigId,
                    fakeLocation = couldBeAFakeLocation,
                    fullAddressFromGps = fullAddressFromGps
            )
        )
    }


    companion object {
        const val TAG = "UserLocationRepository"
    }
}