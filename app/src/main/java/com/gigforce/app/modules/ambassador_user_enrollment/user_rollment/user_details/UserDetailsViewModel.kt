package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.models.City
import com.gigforce.app.modules.ambassador_user_enrollment.models.State
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.UserEnrollmentRepository
import com.gigforce.app.modules.preferences.AppConfigurationRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.putFileOrThrow
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*

class UserDetailsViewModel constructor(
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val enrolledUserListRepository: UserEnrollmentRepository = UserEnrollmentRepository(),
    private val configurationRepository: AppConfigurationRepository = AppConfigurationRepository()
) : ViewModel() {

    var states: List<State> = emptyList()
    var cities: List<City> = emptyList()

    private val _submitUserDetailsState = MutableLiveData<Lse>()
    val submitUserDetailsState: LiveData<Lse> = _submitUserDetailsState

    fun updateUserDetails(
        uid: String,
        phoneNumber: String,
        name: String,
        dateOfBirth: Date,
        gender: String,
        highestQualification: String
    ) = viewModelScope.launch {

        _submitUserDetailsState.postValue(Lse.loading())
        try {
            profileFirebaseRepository.updateUserDetails(
                uid = uid,
                phoneNumber = phoneNumber,
                name = name,
                dateOfBirth = dateOfBirth,
                gender = gender,
                highestQualification = highestQualification
            )
            enrolledUserListRepository.updateUserProfileName(uid, name)
            _submitUserDetailsState.value = Lse.success()
            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value = Lse.error(e.message ?: "Unable to submit user details")
            _submitUserDetailsState.value = null
        }
    }


    fun updateUserCurrentAddressDetails(
        uid: String?,
        pinCode: String,
        addressLine1: String,
        addressLine2: String,
        state: String,
        city: String,
        preferredDistanceInKm: Int,
        readyToChangeLocationForWork: Boolean
    ) = viewModelScope.launch {

        _submitUserDetailsState.postValue(Lse.loading())
        try {

            profileFirebaseRepository.updateCurrentAddressDetails(
                uid = uid,
                pinCode = pinCode,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                state = state,
                city = city,
                preferredDistanceInKm = preferredDistanceInKm,
                readyToChangeLocationForWork = readyToChangeLocationForWork
            )
            _submitUserDetailsState.value = Lse.success()
            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value = Lse.error(e.message ?: "Unable to submit user details")
            _submitUserDetailsState.value = null
        }
    }

    fun uploadProfilePicture(
        userId: String?,
        uri: Uri?,
        data: ByteArray
    ) = viewModelScope.launch {
        _submitUserDetailsState.postValue(Lse.loading())

        Log.v("Upload Image", "started")
        val mReference =
            firebaseStorage.reference.child("profile_pics").child(uri!!.lastPathSegment!!)

        /**
         * Uploading task created and initiated here.
         */
        Log.d("UPLOAD", "uploading files")
        try {
            val taskSnap = mReference.putFileOrThrow(uri)
            val fname: String = taskSnap.metadata?.reference?.name.toString()

            profileFirebaseRepository.setProfileAvatarName(userId, fname)

            if (userId != null) {
                enrolledUserListRepository.updateUserProfilePicture(userId, fname)
            }

            _submitUserDetailsState.value = Lse.success()
            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value =
                Lse.error(e.message ?: "Unable to upload profile picture")
            _submitUserDetailsState.value = null
        }
    }

    private val _profile = MutableLiveData<ProfileData>()
    val profile: LiveData<ProfileData> = _profile

    private var listenerRegistration: ListenerRegistration? = null

    fun startWatchingProfile(userId: String?) {
        listenerRegistration = profileFirebaseRepository.getProfileRef(userId)
            .addSnapshotListener { value, error ->

                error?.printStackTrace()

                value?.let {

                    val profileData = it.toObject(ProfileData::class.java)!!.apply {
                        this.id = it.id
                    }
                    _profile.value = profileData
                }
            }
    }

    private val _citiesAndStateLoadState = MutableLiveData<Lse>()
    val citiesAndStateLoadState: LiveData<Lse> = _citiesAndStateLoadState

    fun loadCityAndStates() = viewModelScope.launch {
        try {
            _citiesAndStateLoadState.value = Lse.loading()
            states = configurationRepository.getStates()
            cities = configurationRepository.getCities()

            _citiesAndStateLoadState.value = Lse.success()
        } catch (e: Exception) {
            _citiesAndStateLoadState.value = Lse.error(e.message!!)
        }
    }


    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}