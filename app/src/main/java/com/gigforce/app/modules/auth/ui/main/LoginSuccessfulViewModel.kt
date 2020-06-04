package com.gigforce.app.modules.auth.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class LoginSuccessfulViewModel() : ViewModel() {
    var profileFirebaseRepository = ProfileFirebaseRepository()

    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()

    fun getProfileData() {
        profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    var errProfileData = ProfileData()
                    errProfileData.status = false
                    errProfileData.errormsg = e.toString()
                    userProfileData.postValue(errProfileData)
                    return@EventListener
                }
                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    Log.d("ProfileViewModel", value!!.data.toString())
                    userProfileData.postValue(
                        value!!.toObject(ProfileData::class.java)
                    )
                }
            })
    }
}