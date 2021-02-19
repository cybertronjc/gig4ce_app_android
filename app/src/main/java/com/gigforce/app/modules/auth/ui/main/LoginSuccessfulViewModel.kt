package com.gigforce.app.modules.auth.ui.main

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.BuildConfig
import com.gigforce.app.modules.gigPage.GigsRepository
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import java.util.*

data class ProfileAnGigInfo(
    val profile: ProfileData,
    val hasGigs: Boolean
)

@Keep
data class UserVersionInfo(
    var currentVersion: String = "",
    var time: Timestamp = Timestamp.now()
)

class LoginSuccessfulViewModel constructor(
    private val gigsRepository: GigsRepository = GigsRepository(),
    private val firebaseFunctions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : ViewModel() {
    var profileFirebaseRepository = ProfileFirebaseRepository()

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
        profileFirebaseRepository
            .db
            .collection("Version_info")
            .document(profileFirebaseRepository.getUID())
            .set(
                UserVersionInfo(
                    currentVersion = BuildConfig.VERSION_NAME
                )
            )
            .addOnSuccessListener {
                Log.d("VersionInfo", "User version added")
            }.addOnFailureListener {
                Log.e("VersionInfo", "unable to add version info", it)
            }



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

    private fun checkForGigData(profileData: ProfileData) {
        gigsRepository.getCurrentUserGigs()
            .get()
            .addOnSuccessListener {
                val gigAvailable = hasGigs(it)

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
    }

    private fun hasGigs(querySnapshot: QuerySnapshot): Boolean {
        val userGigs: MutableList<Gig> = mutableListOf()
        querySnapshot.documents.forEach { t ->
            t.toObject(Gig::class.java)?.let {
                it.gigId = t.id
                userGigs.add(it)
            }
        }

        val currentDate = Date()
        return userGigs.any {
            it.startDateTime!!.toDate().time > currentDate.time
        }
    }

    private fun dumm() {

        firebaseFunctions.getHttpsCallable("getMainScreenRedirectionConfig")
            .call()
            .continueWith {
                val result = it.result?.data as String
                result
            }
            .addOnSuccessListener {
                Log.d("D", "d")
            }
            .addOnFailureListener {

                Log.d("D", "d")
            }

    }
}