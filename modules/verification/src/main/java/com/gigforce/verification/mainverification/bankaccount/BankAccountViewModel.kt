package com.gigforce.verification.mainverification.bankaccount

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.verification.mainverification.Data
import com.gigforce.verification.mainverification.KycOcrResultModel
import com.gigforce.verification.mainverification.KycVerifyReqModel
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class BankAccountViewModel @Inject constructor(
    private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {
    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    var kycOcrResultModel: KycOcrResultModel? = null
    val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult
    val _kycVerifyResult = MutableLiveData<KycOcrResultModel>()
    val kycVerifyResult: LiveData<KycOcrResultModel> = _kycVerifyResult

    val _beneficiaryName = MutableLiveData<String>()
    val beneficiaryName: LiveData<String> = _beneficiaryName

    val _verifiedStatus = MutableLiveData<BankDetailsDataModel>()
    val verifiedStatus: LiveData<BankDetailsDataModel> = _verifiedStatus

    val _verifiedStatusDB = MutableLiveData<Boolean>()
    val verifiedStatusDB: LiveData<Boolean> = _verifiedStatusDB

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
            Log.d("result", kycVerifyResult.toString())
        }
    fun getBeneficiaryName() {
        verificationKycRepo.db.collection("Verification").document(verificationKycRepo.getUID()).addSnapshotListener { value, error ->

            value?.data?.let {
                    val doc = value.toObject(VerificationBaseModel::class.java)
                    doc?.bank_details?.bankBeneficiaryName?.let {
                        if (it.isNotBlank()){
                            _beneficiaryName.value = it
                        }
                    }
            }
        }
    }
//        viewModelScope.launch {
//            try {
//                _beneficiaryName.value = verificationKycRepo.getBeneficiaryName()
//            } catch (e: Exception){
//
//            }
//        }

    fun getVerifiedStatus(){
        viewModelScope.launch {
            try {
                _verifiedStatus.value = verificationKycRepo.getVerificationStatus()?.bank_details
            }catch (e: Exception){
//                _verifiedStatus.value = false
            }
        }
    }

    fun setVerificationStatusInDB(status: Boolean) =
        viewModelScope.launch {
            try {
                _verifiedStatusDB.value = verificationKycRepo.setVerifiedStatus(status)
            }catch (e: Exception){

            }
        }
}