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
        profileFirebaseRepository.getProfile()
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
        ageOptions.add("18-22")
        ageOptions.add("22-26")
        ageOptions.add("26-30")
        ageOptions.add("30-34")
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
        educationOption.add("Diploma")
        educationOption.add("Bachelor’s")
        educationOption.add("Master’s")
        educationOption.add("12th")
        return educationOption
    }
    fun getWorkStatusOptions():ArrayList<String>{
        var workStatus = ArrayList<String>()
        workStatus.add("Fresher")
        workStatus.add("Exprienced")
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
        profileFirebaseRepository.setDataAsKeyValue("isOnboardingCompleted",true)
    }


}