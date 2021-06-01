package com.gigforce.user_tracking.repository

import android.util.Log
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.user_tracking.models.LatLng
import com.gigforce.user_tracking.models.UserLocation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
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
        accuracy: Float,
        gigId: String,
        couldBeAFakeLocation: Boolean,
        fullAddressFromGps: String
    ) {

        if (user == null) {
            Log.d(TAG, "Captured User location but logged in user was null")
            return
        }

        collectionRef.document(gigId)
                .updateOrThrow(mapOf("locations" to FieldValue.arrayUnion(
                        UserLocation(
                                location = GeoPoint(location.latitude, location.longitude),
                                locationAccuracy = accuracy,
                                locationCapturedTime = Timestamp.now(),
                                fakeLocation = couldBeAFakeLocation,
                                fullAddressFromGps = fullAddressFromGps
                        )
                ))
                )
    }


    companion object {
        const val TAG = "UserLocationRepository"
    }
}