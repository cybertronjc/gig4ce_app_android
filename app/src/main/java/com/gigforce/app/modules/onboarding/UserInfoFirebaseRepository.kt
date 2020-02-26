package com.gigforce.app.modules.onboarding

import com.gigforce.app.modules.onboarding.models.UserData
import com.gigforce.app.modules.onboarding.models.UserInfo
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoFirebaseRepository {
    var firebaseDB = FirebaseFirestore.getInstance()
    //var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var uid = "obUsers123" // Test

    fun getUserInfo(): DocumentReference {
        return firebaseDB.collection("user_profiles").document(uid)
    }

    fun setUserInfo(userInfo: UserData) {
            firebaseDB.collection("user_profiles")
                .document(uid).update(
                    mapOf<String, Any> (
                    "name" to userInfo
                    /*"dob" to userInfo.dob,
                    "gender" to userInfo.gender,
                    "qualification" to userInfo.qualification
                    "company" to iUInfo.company,
                    "daysOfWork" to iUInfo.daysofwork,
                     "yoq" to iUInfo.yoq,
                     "hoursOfWork" to iUInfo.hoursofwork,
                     "studentOrWorker" to iUInfo.isStudent,
                     "role" to iUInfo.role,
                     "profilePic" to iUInfo.profilePic,
                     "yoe" to iUInfo.yoe*/
                )
                )
    }
}
