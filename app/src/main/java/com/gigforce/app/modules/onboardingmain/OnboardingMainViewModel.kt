package com.gigforce.app.modules.onboardingmain

import androidx.lifecycle.ViewModel

class OnboardingMainViewModel : ViewModel() {

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
}