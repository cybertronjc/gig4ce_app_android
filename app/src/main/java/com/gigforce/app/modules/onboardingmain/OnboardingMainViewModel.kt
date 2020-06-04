package com.gigforce.app.modules.onboardingmain

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

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
                    userProfileData.postValue(
                        value!!.toObject(ProfileData::class.java)
                    )
                }
            })
    }



    fun getOnboardingData():ArrayList<ArrayList<String>>{
        var datalist: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
        datalist.add(ArrayList<String>())
        datalist.add(getAgeOptions())
        datalist.add(getGenderOptions())
        datalist.add(getEducationOption())
        datalist.add(getWorkStatusOptions())
        return datalist
    }
    fun getAgeOptions():ArrayList<String>{
        var ageOptions = ArrayList<String>()
        ageOptions.add("14-18")
        ageOptions.add("18-22")
        ageOptions.add("22-26")
        ageOptions.add("26-30")
        ageOptions.add("30-34")
        ageOptions.add("34-38")
        ageOptions.add("38-42")
        ageOptions.add("42-46")
        ageOptions.add("46-50")
        ageOptions.add("50-54")
        ageOptions.add("54-58")
        ageOptions.add("58-62")
        ageOptions.add("62-66")
        ageOptions.add("66-70")
        ageOptions.add("70-74")
        ageOptions.add("74-78")
        ageOptions.add("78-82")
        ageOptions.add("82-86")
        ageOptions.add("86-90")
        ageOptions.add("90-94")
        ageOptions.add("94-98")

        return ageOptions
    }
    fun getGenderOptions():ArrayList<String>{
        var genderOption = ArrayList<String>()
        genderOption.add("Male")
        genderOption.add("Female")
        genderOption.add("Other")
        return genderOption
    }
    fun getEducationOption():ArrayList<String>{
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
    fun getWorkStatusOptions():ArrayList<String>{
        var workStatus = ArrayList<String>()
        workStatus.add("Fresher")
        workStatus.add("Experienced")
        return workStatus
    }

    fun saveUserName(username: String) {
        profileFirebaseRepository.setDataAsKeyValue("name",username)
    }

    fun saveAgeGroup(ageGroup: String) {
        profileFirebaseRepository.setDataAsKeyValue("ageGroup",ageGroup)


    }

    fun selectYourGender(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("gender",selectedDataFromRecycler)
    }

    fun saveHighestQualification(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("highestEducation",selectedDataFromRecycler)
    }

    fun saveWorkStatus(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("workStatus",selectedDataFromRecycler)
    }

    fun setOnboardingCompleted() {
        profileFirebaseRepository.setDataAsKeyValue("isOnboardingCompletedOnce","true")
    }


}