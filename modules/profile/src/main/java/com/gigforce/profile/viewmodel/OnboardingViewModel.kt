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

    private val _submitUserDetailsState = MutableLiveData<Lse>()
    val submitUserDetailsState: LiveData<Lse> = _submitUserDetailsState

    fun uploadProfilePicture(
            uri: Uri
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
            profileFirebaseRepository.setProfileAvatarName(fname, thumnailNameOnServer)

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


    private val _majorCities = MutableLiveData<List<CityWithImage>>()
    val majorCities: LiveData<List<CityWithImage>> = _majorCities

    fun getAllMajorCities() {

        val majorCities = listOf(
                CityWithImage(
                        id = "QzBMWPULA1ucG0AJyiQO",
                        name = "Chandigarh",
                        image = R.drawable.ic_chandigarh,
                        stateCode = "in_chandigarh"
                ),
                CityWithImage(
                        id = "HCbEvKJd2aPZaYgenUV7",
                        name = "Delhi-Ncr",
                        image = R.drawable.ic_delhi,
                        stateCode = "in_delhi"
                ),
                CityWithImage(
                        id = "hs1qTRWpJMbZXLD0wGVt",
                        name = "Hyderabad",
                        image = R.drawable.ic_hyderabad,
                        stateCode = "in_telangana"
                ),
                CityWithImage(
                        id = "77saiy6h46gisSOejIZV",
                        name = "Mumbai",
                        image = R.drawable.ic_mumbai,
                        stateCode = "in_maharashtra"
                ),
                CityWithImage(
                        id = "eoWQ1EOLP4phzZjuGffz",
                        name = "Jaipur",
                        image = R.drawable.ic_jaipur,
                        stateCode = "in_rajasthan"
                ),
                CityWithImage(
                        id = "edjFfvRTHwZOfKzKQ73E",
                        name = "Chennai",
                        image = R.drawable.ic_chennai,
                        stateCode = "in_tamil_nadu"
                ),
                CityWithImage(
                        id = "gIRzevfWSVwpcASTphdo",
                        name = "Bangalore",
                        image = R.drawable.ic_banglore,
                        stateCode = "in_karnataka"
                ),
                CityWithImage(
                        id = "HXvsMILyHN1X3V1Xq1gM",
                        name = "Kolkata",
                        image = R.drawable.ic_kolkata,
                        stateCode = "in_west_bengal"
                ),
                CityWithImage(
                        id = "Z4s7CqA6IgCBTJp74dAv",
                        name = "Guwahati",
                        image = R.drawable.ic_guwahati,
                        stateCode = "in_assam"
                ),
                CityWithImage(
                        id = "wEFMHm7vBmXx1Kd7kEqE",
                        name = "Lucknow",
                        image = R.drawable.ic_lucknow,
                        stateCode = "in_uttar_pradesh"
                ),
                CityWithImage(
                        id = "CtSrJSbu5AP6CfdMu2kI",
                        name = "Pune",
                        image = R.drawable.ic_pune,
                        stateCode = "in_maharashtra"
                )
        )

        _majorCities.postValue(
                majorCities
        )
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

    override fun onCleared() {
        super.onCleared()
    }
}