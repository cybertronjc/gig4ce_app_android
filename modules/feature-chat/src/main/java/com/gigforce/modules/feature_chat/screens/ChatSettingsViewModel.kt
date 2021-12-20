package com.gigforce.modules.feature_chat.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.remote.verification.KycOcrResultModel
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatSettingsViewModel@Inject constructor(
    private val profileFirebaseRepository: ProfileFirebaseRepository
): ViewModel() {

    val _autoDownload = MutableLiveData<Boolean>()
    val autoDownload: LiveData<Boolean> = _autoDownload

     fun updateMediaAutoDownloadInDB(enable: Boolean) {
        FirebaseAuth.getInstance().currentUser?.let { it ->
            FirebaseFirestore
                .getInstance()
                .collection("Profiles").document(it.uid).update(mapOf("mediaAutoDownload" to enable, "updatedAt" to Timestamp.now(), "updatedBy" to it.uid)).addOnFailureListener { exception ->
                    FirebaseCrashlytics.getInstance().log("Exception : updateMediaAutoDownloadInDB Method $exception")
                }
        }
    }

    fun getMediaAutoDownload() {
        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseFirestore
                .getInstance()
                .collection("Profiles").document(it.uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        val profileData = it.toObject(ProfileData::class.java)
                            ?: throw  IllegalStateException("unable to parse profile object")
                        if (profileData?.mediaAutoDownload){
                            _autoDownload.value = true
                        } else {
                            _autoDownload.value = false
                        }
                    }
                }
        }
    }

}