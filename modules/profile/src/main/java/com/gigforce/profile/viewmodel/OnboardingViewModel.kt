package com.gigforce.profile.viewmodel

import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.putBytesOrThrow
import com.gigforce.core.extensions.putFileOrThrow
import com.gigforce.core.image.ImageUtils
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.profile.R
import com.gigforce.profile.models.City
import com.gigforce.profile.models.CityWithImage
import com.gigforce.profile.models.OnboardingProfileData
import com.gigforce.profile.onboarding.fragments.interest.InterestDM
import com.gigforce.profile.repository.OnboardingProfileFirebaseRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class OnboardingViewModel constructor(
        private val profileFirebaseRepository: OnboardingProfileFirebaseRepository = OnboardingProfileFirebaseRepository(),
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
        private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _submitUserDetailsState = MutableLiveData<Lce<String>>()
    val submitUserDetailsState: LiveData<Lce<String>> = _submitUserDetailsState

    fun uploadProfilePicture(
            uri: Uri
    ) = viewModelScope.launch(Dispatchers.IO) {
        _submitUserDetailsState.postValue(Lce.loading())

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
            profileFirebaseRepository.setProfileAvatarName(fname, thumnailNameOnServer)

            _submitUserDetailsState.postValue(Lce.content(fname))
//            _submitUserDetailsState.postValue(null)
//            val urlTask = taskSnap.task.continueWith { task ->
//                if (!task.isSuccessful) {
//                    task.exception?.let {
//                        throw it
//                    }
//                }
//                mReference.downloadUrl
//            }.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val downloadUri = task.result
//                    Log.d("image url", downloadUri.result.toString())
//                } else {
//                    // Handle failures
//                    // ...
//                    Log.d("image url", "failure")
//                }
//            }

            Log.v("ProfilePicture", "Success")
        } catch (e: Exception) {
            Log.v("ProfilePicture", "error")
            e.printStackTrace()

            FirebaseCrashlytics.getInstance().log("Error while uploading profile pic")
            FirebaseCrashlytics.getInstance().recordException(e)

            _submitUserDetailsState.postValue(
                    Lce.error(e.message ?: "Unable to upload profile picture")
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


    private val _profile = MutableLiveData<Lce<OnboardingProfileData>>()
    val profile: LiveData<Lce<OnboardingProfileData>> = _profile

    fun getProfileForUser() = viewModelScope.launch {
        try {
            _profile.value = Lce.loading()
            val profileData = profileFirebaseRepository.getProfileData()

            _profile.value = Lce.content(profileData)
        } catch (e: Exception) {
            _profile.value = Lce.error(e.message!!)
        }
    }


    private val _majorCities = MutableLiveData<ArrayList<CityWithImage>>()
    val majorCities: LiveData<ArrayList<CityWithImage>> = _majorCities

    fun getAllMajorCities()  = viewModelScope.launch{

        try {
            val majorCityData =  ArrayList<CityWithImage>()
            firebaseFirestore
                .collection("Mst_Cities").whereEqualTo("majorCity", true)
                .addSnapshotListener { value, error ->
                    error?.printStackTrace()

                    value.let {
                        it?.documents?.forEach { majorCity ->
                            majorCity.toObject(CityWithImage::class.java).let {
                                it?.id = majorCity.id
                                if (it != null) {
                                    majorCityData.add(it)
                                }
                            }
                        }
                    }
                    majorCityData.sortBy { it -> it.index }
                    _majorCities.value = majorCityData
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _allCities = MutableLiveData<List<City>>()
    val allCities: LiveData<List<City>> = _allCities

    fun getAllCities() = viewModelScope.launch {

        try {
            val citiesQuery = firebaseFirestore
                    .collection("Mst_Cities")
                    .getOrThrow()

            _allCities.value = citiesQuery.documents.map {
                it.toObject(City::class.java)!!.apply {
                    this.id = it.id
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _subCities = MutableLiveData<ArrayList<String>>()
    val subCities: LiveData<ArrayList<String>> = _subCities

    fun getSubCities(stateCode: String, cityCode: String)  = viewModelScope.launch{

        try {
            val subCityData =  ArrayList<String>()
            firebaseFirestore
                .collection("MST_Sublocations").whereEqualTo("state_code", stateCode).whereEqualTo("cityCode", cityCode)
                .addSnapshotListener { value, error ->
                    error?.printStackTrace()

                    value.let {
                        it?.documents?.forEach {
                            Log.d("documentsnap", it.toString())
                            if (it != null){
                                var name = it.getString("name")
                                if (name != null) {
                                    Log.d("subcity", name)
                                    subCityData.add(name)
                                }
                            }
                        }
//                                subCity.toObject(CityWithImage::class.java).let {
//                                it?.id = subCity.id
//                                if (it != null) {
//                                    subCityData.add(it)
//                                }

                        }

                    _subCities.value = subCityData
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun savePreferredJobLocation(
            cityId: String,
            cityName: String,
            stateCode : String,
            subLocation: List<String>
    ) = viewModelScope.launch {

        try {
            profileFirebaseRepository.setPreferredJobLocation(
                    cityId,
                    cityName,
                    stateCode,
                    subLocation
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveLeadSource(
        leadSource: HashMap<String, String>
    ) = viewModelScope.launch {
        try {
            profileFirebaseRepository.setLeadSource(leadSource)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}