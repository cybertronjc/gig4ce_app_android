package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details

import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.repository.ProfileFirebaseRepository
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.common_ui.configrepository.AppConfigurationRepository
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.ambassador.PincodeResponse
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.UserEnrollmentRepository
import com.gigforce.core.utils.EventLogs.putBytesOrThrow
import com.gigforce.core.utils.EventLogs.putFileOrThrow
import com.gigforce.core.utils.ImageUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val buildConfig: IBuildConfigVM
) : ViewModel() {

    private val profileFirebaseRepository: ProfileFirebaseRepository =
        ProfileFirebaseRepository()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val enrolledUserListRepository: UserEnrollmentRepository =
        UserEnrollmentRepository(buildConfig = buildConfig)
    private val configurationRepository: AppConfigurationRepository = AppConfigurationRepository()
    private val userEnrollmentRepository: UserEnrollmentRepository =
        UserEnrollmentRepository(buildConfig = buildConfig)

    var states: List<State> = emptyList()
    var cities: List<City> = emptyList()

    private val _submitUserDetailsState = MutableLiveData<Lse>()
    val submitUserDetailsState: LiveData<Lse> = _submitUserDetailsState
    val _pincodeResponse = MutableLiveData<Lce<PincodeResponse>>()
    val pincodeResponse: LiveData<Lce<PincodeResponse>> = _pincodeResponse

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
            //  enrolledUserListRepository.setUserDetailsAsFilled(uid)

            _submitUserDetailsState.value = Lse.success()
//            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value = Lse.error(e.message ?: "Unable to submit user details")
//            _submitUserDetailsState.value = null
        }
    }


    fun updateUserCurrentAddressDetails(
            uid: String?,
            pinCode: String,
            addressLine1: String,
            addressLine2: String,
            state: String,
            city: String,
            homeCity: String = "",
            homeState: String = ""
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
                    homeCity = homeCity,
                    homeState = homeState
            )

            if (uid != null)
                userEnrollmentRepository.setCurrentAddressAsUploaded(uid)

            _submitUserDetailsState.value = Lse.success()
//            _submitUserDetailsState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _submitUserDetailsState.value = Lse.error(e.message ?: "Unable to submit user details")
//            _submitUserDetailsState.value = null
        }
    }

    fun uploadProfilePicture(
        userId: String?,
        uri: Uri?,
        data: ByteArray
    ) = viewModelScope.launch(Dispatchers.IO) {
        _submitUserDetailsState.postValue(Lse.loading())

        Log.v("ProfilePicture", "started")
        val mReference =
            firebaseStorage.reference.child("profile_pics").child(uri!!.lastPathSegment!!)

        /**
         * Uploading task created and initiated here.
         */
        Log.d("UPLOAD", "uploading files")
        try {
            val taskSnap = mReference.putFileOrThrow(uri)
            val fname: String = taskSnap.metadata?.reference?.name.toString()

            val thumnailNameOnServer = uploadThumbnailAndReturnNameOnServer(fname, uri)
            profileFirebaseRepository.setProfileAvatarName(userId, fname, thumnailNameOnServer)

            if (userId != null) {
                enrolledUserListRepository.updateUserProfilePicture(
                    userId,
                    fname,
                    thumnailNameOnServer
                )
                enrolledUserListRepository.setProfilePictureAsUploaded(userId)
            }

            _submitUserDetailsState.postValue(Lse.success())
//            _submitUserDetailsState.postValue(null)

            Log.v("ProfilePicture", "Sucess")
        } catch (e: Exception) {
            Log.v("ProfilePicture", "error")
            e.printStackTrace()

            FirebaseCrashlytics.getInstance().log("Error while uploading profile pic")
            FirebaseCrashlytics.getInstance().recordException(e)

            _submitUserDetailsState.postValue(
                Lse.error(e.message ?: "Unable to upload profile picture")
            )
            //           _submitUserDetailsState.postValue(null)
        }
    }

    private suspend fun uploadThumbnailAndReturnNameOnServer(fname: String, uri: Uri): String? {

        return try {

            val thumbnail =
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        ThumbnailUtils.createImageThumbnail(File(uri.path), Size(156, 156), null)
                    } else {
                        ImageUtils.resizeBitmap(uri.path!!, 156, 156)
                    }
                } catch (e: Exception) {
                    null
                }
            if (thumbnail != null) {
                val imageInBytes = ImageUtils.convertToByteArray(thumbnail)

                val thumbnailName =
                    fname.substringBeforeLast(".") + "_thumbnail." + fname.substringAfterLast(".")
                val mReference =
                    firebaseStorage.reference.child("profile_pics").child(thumbnailName)
                mReference.putBytesOrThrow(imageInBytes)
                thumbnailName
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    private var listenerRegistration: ListenerRegistration? = null

    fun startWatchingProfile(userId: String?) {
        listenerRegistration = profileFirebaseRepository.getProfileRef(userId)
            .addSnapshotListener { value, error ->
                error?.printStackTrace()

                value?.let {

                    val profileData = it.toObject(ProfileData::class.java)!!.apply {
                        this.id = it.id
                    }
                    _profile.value = Lce.content(profileData)
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

    private val _profile = MutableLiveData<Lce<ProfileData>>()
    val profile: LiveData<Lce<ProfileData>> = _profile

    fun getProfileForUser(
        userId: String?
    ) = viewModelScope.launch {
        try {
            _profile.value = Lce.loading()
            val profileData = profileFirebaseRepository.getProfileData(
                userId = userId
            )

            _profile.value = Lce.content(profileData)
        } catch (e: Exception) {
            _profile.value = Lce.error(e.message!!)
        }
    }

    fun loadCityAndStateUsingPincode(pinCode: String) = viewModelScope.launch {
        try {
            val cityStateData = enrolledUserListRepository.loadCityAndStateUsingPincode(pinCode)
            _pincodeResponse.value = Lce.content(cityStateData)
        } catch (e: Exception) {
            _pincodeResponse.value = Lce.error("Some Error occured!!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}