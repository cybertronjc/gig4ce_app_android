package com.gigforce.ambassador.user_rollment.kycdocs.aadhardetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.ambassador.user_rollment.kycdocs.KycOcrResultModel
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationKycRepo
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.datamodels.profile.ProfileNominee
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.IAadhaarDetailsRepository
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UserAadharDetailInfoViewModel @Inject constructor( private val iBuildConfigVM: IBuildConfigVM) : ViewModel() {
    private val aadharDetailsRepo: UserAadharDetailRepository = UserAadharDetailRepository()
    val statesResult: MutableLiveData<MutableList<State>> = MutableLiveData<MutableList<State>>()
    val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult
    val verificationResult: MutableLiveData<VerificationBaseModel> = MutableLiveData<VerificationBaseModel>()
    val caCitiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()
    val citiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()
    val updatedResult: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val _observableAddApplicationSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val observableAddApplicationSuccess: MutableLiveData<Boolean> = _observableAddApplicationSuccess
    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    val _profileNominee : MutableLiveData<ProfileNominee> = MutableLiveData<ProfileNominee>()
    val profileNominee : LiveData<ProfileNominee> = _profileNominee
    fun getStates() = viewModelScope.launch {
        try {
            val states = aadharDetailsRepo.getStatesFromDb()
            statesResult.postValue(states)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getKycOcrResult(uid : String,type: String, subType: String, image: MultipartBody.Part) =
        viewModelScope.launch {
            try {
                _kycOcrResult.value = verificationKycRepo.getVerificationOcrResult(uid,type, subType, image)
            } catch (e: Exception) {
                _kycOcrResult.value = KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", _kycOcrResult.toString())
        }

    fun getVerificationData(uid: String) = viewModelScope.launch {
        try {
            val veriData = aadharDetailsRepo.getVerificationDetails(uid)
            verificationResult.postValue(veriData)
            val profileNominee = aadharDetailsRepo.getProfileNominee(uid)
            _profileNominee.postValue(profileNominee)
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
    fun setAadhaarDetails(uid: String,submitDataModel: AadhaarDetailsDataModel, nomineeAsFather : Boolean, mJobProfileId : String)= viewModelScope.launch {
        try {
            val updated = aadharDetailsRepo.setAadhaarFromVerificationModule(uid,nomineeAsFather, submitDataModel)
            updatedResult.postValue(updated)
            if (mJobProfileId.isNotEmpty()){
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
            }

        }catch (e:Exception){
            updatedResult.postValue(false)
        }
    }
}