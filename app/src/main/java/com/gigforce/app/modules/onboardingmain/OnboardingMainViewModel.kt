package com.gigforce.app.modules.onboardingmain

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.EnrollmentInfo
import com.gigforce.app.modules.profile.models.ErrorWhileSettingUserAsAmbassador
import com.gigforce.app.modules.profile.models.Invites
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

class OnboardingMainViewModel : ViewModel() {

    var profileFirebaseRepository = ProfileFirebaseRepository()

    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()

    init {
        getProfileData()
    }

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
                    val obj = value!!.toObject(ProfileData::class.java)
                    obj?.id = value.id
                    userProfileData.postValue(obj)
                }
            })
    }


    fun getOnboardingData(): ArrayList<OnboardingData> {
        var datalist: ArrayList<OnboardingData> = ArrayList<OnboardingData>()
        datalist.add(OnboardingData(ArrayList<String>(), 0))
        datalist.add(OnboardingData(getAgeOptions(), 1))
        datalist.add(OnboardingData(getGenderOptions(), 0))
        datalist.add(OnboardingData(getEducationOption(), 5))
        datalist.add(OnboardingData(getWorkStatusOptions(), 0))
        return datalist
    }

    class OnboardingData(var data: ArrayList<String>, var defaultValue: Int = 0) {

    }

    fun getAgeOptions(): ArrayList<String> {
        var ageOptions = ArrayList<String>()
        ageOptions.add("14-18")
        ageOptions.add("19-23")
        ageOptions.add("24-28")
        ageOptions.add("29-33")
        ageOptions.add("34-38")
        ageOptions.add("39-43")
        ageOptions.add("44-48")
        ageOptions.add("49-53")
        ageOptions.add("54-58")
        ageOptions.add("59-63")
        ageOptions.add("64-68")
        ageOptions.add("69-73")
        ageOptions.add("74-78")
        ageOptions.add("79-83")
        ageOptions.add("84-88")
        ageOptions.add("89-93")
        ageOptions.add("94-82")

        return ageOptions
    }

    fun getGenderOptions(): ArrayList<String> {
        var genderOption = ArrayList<String>()
        genderOption.add("Male")
        genderOption.add("Female")
        genderOption.add("Other")
        return genderOption
    }

    fun getEducationOption(): ArrayList<String> {
        var educationOption = ArrayList<String>()
        educationOption.add("<10th")
        educationOption.add("10th")
        educationOption.add("12th")
        educationOption.add("Certificate")
        educationOption.add("Diploma")
        educationOption.add("Bachelor's")
        educationOption.add("Masters")
        educationOption.add("Doctorate")
        educationOption.add("Others")
        return educationOption
    }

    fun getWorkStatusOptions(): ArrayList<String> {
        var workStatus = ArrayList<String>()
        workStatus.add("Fresher")
        workStatus.add("Experienced")
        return workStatus
    }

    fun saveUserName(username: String) {
        profileFirebaseRepository.setDataAsKeyValue("name", username)
    }

    fun saveAgeGroup(ageGroup: String) {
        profileFirebaseRepository.setDataAsKeyValue("ageGroup", ageGroup)


    }

    fun selectYourGender(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("gender", selectedDataFromRecycler)
    }

    fun saveHighestQualification(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("highestEducation", selectedDataFromRecycler)
    }

    fun saveWorkStatus(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("workStatus", selectedDataFromRecycler)
    }

    fun setOnboardingCompleted(
        invite: String?,
        inviteByAmbassador: String,
        roleID: String,
        jobProfileId: String
    ) {
        if (!invite.isNullOrEmpty()) {
            var listener: ListenerRegistration? = null
            listener = profileFirebaseRepository.getCollectionReference()
                .document(invite).addSnapshotListener { snapshot, err ->
                    listener?.remove()
                    run {
                        val obj = snapshot?.toObject(ProfileData::class.java) ?: return@run
                        if (obj?.invited == null) {
                            profileFirebaseRepository.getCollectionReference()
                                .document(invite)
                                .update(
                                    "invited",
                                    arrayListOf(
                                        Invites(
                                            profileFirebaseRepository.getUID(),
                                            Date(),
                                            roleID,
                                            jobProfileId
                                        )
                                    )
                                )

//                                    EnrolledUser(uid = profileFirebaseRepository.uid,enrolledBy = invite, enrolledByLink = true )
                        } else {
                            profileFirebaseRepository.getCollectionReference()
                                .document(invite)
                                .update(
                                    "invited",
                                    FieldValue.arrayUnion(
                                        Invites(
                                            profileFirebaseRepository.getUID(),
                                            Date(), roleID, jobProfileId
                                        )
                                    )
                                )

                        }
                        if (inviteByAmbassador.isNotBlank()) {

                            profileFirebaseRepository.getDBCollection()
                                .update(
                                    "enrolledBy",
                                    EnrollmentInfo(id = invite, enrolledOn = Timestamp.now())

                                )

                            profileFirebaseRepository.db.collection("Ambassador_Enrolled_User")
                                .document(invite).collection("Enrolled_Users")
                                .document(profileFirebaseRepository.uid).set(
                                    mapOf(
                                        "uid" to profileFirebaseRepository.uid,
                                        "enrolledBy" to invite,
                                        "enrolledOn" to Timestamp.now(),
                                        "enrolledByLink" to true
                                    )
                                )
                        }

                    }
                }


        }

        profileFirebaseRepository.setDataAsKeyValue("isonboardingdone", true)
    }


}