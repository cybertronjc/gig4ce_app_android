package com.gigforce.client_activation.client_activation.info.aadhardetailform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.repository.AadhaarDetailsRepository
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.KYCdata
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AadharApplicationDetailsViewModel @Inject constructor(
    private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {

    val aadharDetailsRepo = AadhaarDetailsRepository()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    val profileFirebaseRepository = ProfileFirebaseRepository()

    val _statesResult = MutableLiveData<State>()
    val statesResult: MutableLiveData<MutableList<State>> = MutableLiveData<MutableList<State>>()

    val _citiesResult = MutableLiveData<City>()
    val citiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()

    val caCitiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()

    val verificationResult: MutableLiveData<VerificationBaseModel> = MutableLiveData<VerificationBaseModel>()
    val updatedResult: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    val addressResult: MutableLiveData<AddressModel> = MutableLiveData<AddressModel>()

    private val _observableAddApplicationSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val observableAddApplicationSuccess: MutableLiveData<Boolean> = _observableAddApplicationSuccess

     fun getStates() = viewModelScope.launch {
        try {
            val states = aadharDetailsRepo.getStatesFromDb()
            statesResult.postValue(states)
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

    fun getVerificationData() = viewModelScope.launch {
        try {
            val veriData = aadharDetailsRepo.getVerificationDetails()
            verificationResult.postValue(veriData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAddressData() = viewModelScope.launch {
        try {
            val addressData = profileFirebaseRepository.getProfileData(userId = uid).address.home
            addressResult.postValue(addressData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAadhaarDetails(data: AadhaarDetailsDataModel,mJobProfileId : String) = viewModelScope.launch {
        try {
            val updated = aadharDetailsRepo.setAadhaarDetails(uid, data)
            updatedResult.postValue(updated)

            FirebaseFirestore.getInstance().collection("JP_Applications")
                .whereEqualTo("jpid", mJobProfileId)
                .whereEqualTo(
                    "gigerId",
                    FirebaseAuthStateListener.getInstance().getCurrentSignInUserInfoOrThrow().uid
                ).addSnapshotListener { jp_application, _ ->

                    jp_application?.let {
                        val jpApplication =
                            it.toObjects(JpApplication::class.java)[0]
                        jpApplication.application.forEach { draft ->
                            if (draft.type == "aadhar_card_questionnaire") {
                                draft.isDone = true
                            }
                        }
                        FirebaseFirestore.getInstance().collection("JP_Applications")
                            .document(jp_application.documents[0].id)
                            .update("application", jpApplication.application)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    _observableAddApplicationSuccess.value =
                                        true
                                }
                            }
                    }
                }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}