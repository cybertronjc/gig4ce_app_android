package com.gigforce.app.modules.auth.ui.main

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.BuildConfig
import com.gigforce.common_ui.ext.toDate
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProfileAnGigInfo(
    val profile: ProfileData,
    val hasGigs: Boolean
)

@Keep
data class UserVersionInfo(
    var currentVersion: String = "",
    var time: Timestamp = Timestamp.now(),
    var uid: String? = FirebaseAuthStateListener.getInstance()
        .getCurrentSignInUserInfoOrThrow().uid,
    var updatedAt: Timestamp? = Timestamp.now(),
    var updatedBy: String? = null,
    var createdAt: Timestamp? = Timestamp.now()
)

@HiltViewModel
class LoginSuccessfulViewModel @Inject constructor(
    private val gigsRepository: GigsRepository,
) : ViewModel() {
    var profileFirebaseRepository =
        ProfileFirebaseRepository()

    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var userProfileAndGigData: MutableLiveData<ProfileAnGigInfo> =
        MutableLiveData<ProfileAnGigInfo>()

    fun getProfileData(
        latitude: Double,
        longitude: Double,
        locationAddress: String
    ) = viewModelScope.launch {
        try {
            val profile =
                profileFirebaseRepository.updateLoginInfoIfUserProfileExistElseCreateProfile(
                    latitude, longitude, locationAddress
                )
            userProfileData.postValue(profile)
        } catch (e: Exception) {
            val errProfileData = ProfileData()
            errProfileData.status = false
            errProfileData.errormsg = e.toString()
            userProfileData.postValue(errProfileData)
        }
    }


    fun getProfileAndGigData() {
        profileFirebaseRepository.db.collection("Version_info")
            .whereEqualTo("currentVersion", BuildConfig.VERSION_NAME)
            .whereEqualTo(
                "uid",
                FirebaseAuthStateListener.getInstance().getCurrentSignInUserInfoOrThrow().uid
            )
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    insertDataToDB()
                }
            }
            .addOnFailureListener {
                insertDataToDB()
            }

//        profileFirebaseRepository
//            .db
//            .collection("Version_info")
//            .document(profileFirebaseRepository.getUID())
//            .set(
//                UserVersionInfo(
//                    currentVersion = BuildConfig.VERSION_NAME
//                )
//            )
//            .addOnSuccessListener {
//                Log.d("VersionInfo", "User version added")
//            }.addOnFailureListener {
//                Log.e("VersionInfo", "unable to add version info", it)
//            }


        profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    var errProfileData = ProfileData()
                    errProfileData.status = false
                    errProfileData.errormsg = e.toString()
                    userProfileAndGigData.postValue(
                        ProfileAnGigInfo(
                            profile = errProfileData,
                            hasGigs = false
                        )
                    )
                    return@EventListener
                }

                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    value.toObject(ProfileData::class.java)?.let {
                        checkForGigData(it)
                    }

                }
            })
    }

    private fun insertDataToDB() {

        //            .document(profileFirebaseRepository.getUID())
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            profileFirebaseRepository
                .db
                .collection("Version_info")
                .add(
                    UserVersionInfo(
                        currentVersion = BuildConfig.VERSION_NAME,
                        uid = it,
                        updatedBy = it
                    )
                )
        }

    }

    private fun checkForGigData(profileData: ProfileData) {
        val yesterday = LocalDate.now().apply {
            minusDays(1)
        }.toDate()
        try {
            gigsRepository.getCurrentUserGigs
                .whereGreaterThan("startDateTime", yesterday)
                .get()
                .addOnSuccessListener {
                    val gigAvailable = it.isEmpty.not()

                    userProfileAndGigData.postValue(
                        ProfileAnGigInfo(
                            profile = profileData,
                            hasGigs = gigAvailable
                        )
                    )

                }.addOnFailureListener {

                    var errProfileData = ProfileData()
                    errProfileData.status = false
                    errProfileData.errormsg = it.message!!
                    userProfileAndGigData.postValue(
                        ProfileAnGigInfo(
                            profile = errProfileData,
                            hasGigs = false
                        )
                    )
                }
        } catch (e: Exception) {

        }
    }
}