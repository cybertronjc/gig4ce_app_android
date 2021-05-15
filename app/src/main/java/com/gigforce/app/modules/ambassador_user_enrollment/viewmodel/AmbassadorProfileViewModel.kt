package com.gigforce.app.modules.ambassador_user_enrollment.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.repo.AmbassadorProfileRepository
import com.gigforce.core.datamodels.profile.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch

class AmbassadorProfileViewModel : ViewModel(){
    var profileFirebaseRepository = AmbassadorProfileRepository()
        private val _viewState = MutableLiveData<ProfileViewStates>()
    val viewState: LiveData<ProfileViewStates> = _viewState


    fun getProfileData(): MutableLiveData<ProfileData> {

        profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener(fun(
                value: DocumentSnapshot?,
                e: FirebaseFirestoreException?
            ) {
                if (e != null) {
                    Log.w("ProfileViewModel", "Listen failed", e)
                    return
                }

                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    Log.d("ProfileViewModel", value!!.data.toString())
                    val obj = value!!.toObject(ProfileData::class.java)
                    obj?.id = value.id;
                    userProfileData.value = obj
                    Log.d("ProfileViewModel", userProfileData.toString())

                    // if user logged in via link
//                    isonboardingdone
//                    profileAvatarName
//                    isProfileUpdatedtoAmbassador

                }
            }))


        return userProfileData
    }


    fun setUserAsAmbassador() = viewModelScope.launch {

        try {
            _viewState.postValue(SettingUserAsAmbassador)

            profileFirebaseRepository.setUserAsAmbassador()
            _viewState.postValue(UserSetAsAmbassadorSuccessfully)
        } catch (e: Exception) {
            e.printStackTrace()

            _viewState.postValue(
                ErrorWhileSettingUserAsAmbassador(
                    e.message ?: "Error while setting user as Ambassador"
                )
            )
        }
    }
}