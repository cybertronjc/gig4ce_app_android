package com.gigforce.app.modules.onboarding

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.onboarding.models.UserInfo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class UserInfoViewModel : ViewModel() {

    var userInfoFirebaseRepository = UserInfoFirebaseRepository()
    var userProfileData: MutableLiveData<UserInfo> = MutableLiveData<UserInfo>()

    fun getUserInfoData(): MutableLiveData<UserInfo> {
        userInfoFirebaseRepository.getProfile().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->
            if (e != null) {
                Log.w("UserInfoViewModel", "Listen failed", e)
                return@EventListener
            }

            Log.d("UserInfoViewModel", value.toString())

            userProfileData.postValue(
                value!!.toObject(UserInfo::class.java)
            )

            Log.d("UserInfoViewModel", userProfileData.toString())
        })
        return userProfileData
    }

    fun setUserProfile(userInfo: ArrayList<UserInfo>) {
        userInfoFirebaseRepository.setUserInfo(userInfo)
    }

    init {
        //uid = FirebaseAuth.getInstance().currentUser?.uid!!
        var uid = "obUsers123" // ob doc
        getUserInfoData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("UserInfoViewModel", "UserInfo View model destroying")
    }
}
