package com.gigforce.app.modules.onboarding

import com.gigforce.app.modules.onboarding.models.UserInfo
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoFirebaseRepository {
    var firebaseDB = FirebaseFirestore.getInstance()
    //var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var uid = "obUsers123" // Test

    fun getProfile(): DocumentReference {
        return firebaseDB.collection("user_profiles").document(uid)
    }

    fun setUserInfo(userInfo: ArrayList<UserInfo>) {
        for(iUInfo in userInfo) {
            firebaseDB.collection("user_profiles")
                .document(uid).update(
                    mapOf<String, Any> (
                     "id" to iUInfo.id,
                    "name" to iUInfo.name,
                    "company" to iUInfo.company,
                    "gender" to iUInfo.gender,
                    "daysOfWork" to iUInfo.daysofwork,
                     "yoq" to iUInfo.yoq,
                     "hoursOfWork" to iUInfo.hoursofwork,
                     "studentOrWorker" to iUInfo.isStudent,
                     "qualification" to iUInfo.qualification,
                     "role" to iUInfo.role,
                     "profilePic" to iUInfo.profilePic,
                     "yoe" to iUInfo.yoe
                )
                )
        }
    }
}
