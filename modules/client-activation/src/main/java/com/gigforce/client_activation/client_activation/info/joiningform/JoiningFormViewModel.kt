package com.gigforce.client_activation.client_activation.info.joiningform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.repository.AadhaarDetailsRepository
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import kotlinx.coroutines.launch

class JoiningFormViewModel : ViewModel() {
    val aadharDetailsRepo = AadhaarDetailsRepository()
    val statesResult: MutableLiveData<MutableList<State>> = MutableLiveData<MutableList<State>>()
    val citiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()

    val caCitiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()

    val verificationResult: MutableLiveData<VerificationBaseModel> = MutableLiveData<VerificationBaseModel>()

    fun getStates()= viewModelScope.launch {
        try {
            val states = aadharDetailsRepo.getStatesFromDb()
            statesResult.postValue(states)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun getVerificationData() = viewModelScope.launch {
        try {
            val veriData = aadharDetailsRepo.getVerificationDetails()
            verificationResult.postValue(veriData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getCities(stateCode: String) = viewModelScope.launch {
        try {
            val cities = aadharDetailsRepo.getCities(stateCode)
            citiesResult.postValue(cities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentAddCities(stateCode: String) = viewModelScope.launch {
        try {
            val cities = aadharDetailsRepo.getCities(stateCode)
            caCitiesResult.postValue(cities)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}