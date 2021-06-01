package com.gigforce.user_tracking.repository

import android.util.Log
import com.gigforce.core.datamodels.profile.ProfileData
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TrackingUserProfileRepository constructor(
        private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getProfileDataIfExist(userId: String): ProfileData? =
        suspendCoroutine { cont ->

            firebaseFirestore.collection("Profile")
                .document(userId)
                .get()
                .addOnSuccessListener {

                    if (it.exists()) {
                        val profileData = it.toObject(ProfileData::class.java)
                            ?: throw  IllegalStateException("unable to parse profile object")
                        profileData.id = it.id
                        cont.resume(profileData)
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }


    companion object {
        const val TAG = "UserLocationRepository"
    }
}