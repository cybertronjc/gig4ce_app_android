package com.gigforce.verification.mainverification.drivinglicense

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.client_activation.States
import com.gigforce.core.datamodels.verification.DrivingLicenseDataModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.verification.mainverification.Data
import com.gigforce.verification.mainverification.KycOcrResultModel
import com.gigforce.verification.mainverification.KycVerifyReqModel
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class DrivingLicenseViewModel  @Inject constructor(
    private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {
    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    var kycOcrResultModel: KycOcrResultModel? = null
    val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult
    val _kycVerifyResult = MutableLiveData<KycOcrResultModel>()
    val kycVerifyResult: LiveData<KycOcrResultModel> = _kycVerifyResult

    val _verifiedStatus = MutableLiveData<DrivingLicenseDataModel>()
    val verifiedStatus: LiveData<DrivingLicenseDataModel> = _verifiedStatus

    private val _observableStates = MutableLiveData<MutableList<States>>()
    val observableStates: MutableLiveData<MutableList<States>> = _observableStates

    private val _observableError: MutableLiveData<String> = MutableLiveData()
    val observableError: MutableLiveData<String> = _observableError

    fun getKycOcrResult(type: String, subType: String, image: MultipartBody.Part) =
        viewModelScope.launch {
            try {
                _kycOcrResult.value =   verificationKycRepo.getVerificationOcrResult(type, subType, image)
            } catch (e: Exception){
                KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", kycOcrResultModel.toString())
        }

    fun getKycVerificationResult(type: String, list: List<Data>) =
        viewModelScope.launch {
            try {
                _kycVerifyResult.value = verificationKycRepo.getKycVerification(type, list)
            }catch (e: Exception){
                KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", kycOcrResultModel.toString())
        }

    fun getVerifiedStatus(){
        viewModelScope.launch {
            try {
                _verifiedStatus.value = verificationKycRepo.getVerificationStatus()?.driving_license
            }catch (e: Exception){
//                _verifiedStatus.value = false
            }
        }
    }

    fun getStates() =
        viewModelScope.launch {
            try {
                _observableStates.value = verificationKycRepo.getStatesFromDb()
            } catch (e: Exception){
                _observableError.value = e.message
            }
        }
}