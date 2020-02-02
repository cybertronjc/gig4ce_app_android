package com.gigforce.app.modules.profile

import com.gigforce.app.modules.profile.models.Education
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    //var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

    fun getProfile(): DocumentReference {
        return firebaseDB.collection("user_profiles").document(uid)
    }

    fun setProfile(education: ArrayList<Education>) {
        var updates: ArrayList<Map<String, Any>> = ArrayList()
        for(ed in education) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Education", FieldValue.arrayUnion(
                    mapOf<String, Any>(
                        "institution" to ed.institution,
                        "course" to ed.course,
                        "degree" to ed.degree,
                        "startYear" to ed.startYear!!,
                        "endYear" to ed.endYear!!
                    )
                ))
        }
    }
}