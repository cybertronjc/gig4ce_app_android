package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class InterestAndExperienceViewModel constructor(
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private val _submitInterestState = MutableLiveData<Lse>()
    val submitInterestState: LiveData<Lse> = _submitInterestState

    fun submitInterests(
        uid: String,
        interests: List<String>
    ) = viewModelScope.launch {

        _submitInterestState.postValue(Lse.loading())
        try {
            profileFirebaseRepository.submitInterest(
                uid = uid,
                interest = interests
            )
            _submitInterestState.value = Lse.success()
            _submitInterestState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitInterestState.value = Lse.error(e.message ?: "Unable to submit interest")
            _submitInterestState.value = null
        }
    }

    private val _saveExpAndReturnNextOne = MutableLiveData<Lce<String?>>()
    val saveExpAndReturnNextOne: LiveData<Lce<String?>> = _saveExpAndReturnNextOne

    fun saveExpAndReturnNewOne(userId : String,experience: Experience) = viewModelScope.launch {
        profileFirebaseRepository
            .submitExperience(userId,experience)
            .addOnSuccessListener {
                checkForPendingInterestExperience(userId)
            }
            .addOnFailureListener {
                _saveExpAndReturnNextOne.value = Lce.error("Unable to save Exp : ${it.message}")
            }
    }

    private fun checkForPendingInterestExperience(userId: String) = viewModelScope.launch {
        try {
            val profileData = profileFirebaseRepository.getProfileData(userId)

            val filledExps = profileData.experiences!!.map { it.title }
            val pendingInts = profileData.interests?.filter {
                !filledExps.contains(it.name)
            } ?: emptyList()

            if (pendingInts.isEmpty()) {
                _saveExpAndReturnNextOne.value = Lce.content(null)
            } else {
                _saveExpAndReturnNextOne.value = Lce.content(pendingInts.first().name)
            }
        } catch (e: Exception) {
            _saveExpAndReturnNextOne.value = Lce.error("Unable to save Exp : ${e.message}")
        }
    }

    private val _experience = MutableLiveData<Lce<String?>>()
    val experience: LiveData<Lce<String?>> = _experience

    fun getPendingInterestExperience(userId : String) = viewModelScope.launch {
        try {
            val profileData = profileFirebaseRepository.getProfileData(userId)

            val filledExps = profileData.experiences!!.map { it.title }
            val pendingInts = profileData.interests?.filter {
                !filledExps.contains(it.name)
            } ?: emptyList()

            if (pendingInts.isEmpty()) {
                _experience.value = Lce.content(null)
            } else {
                _experience.value = Lce.content(pendingInts.first().name)
            }
        } catch (e: Exception) {
            _experience.value = Lce.error("Unable to get Exp : ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}