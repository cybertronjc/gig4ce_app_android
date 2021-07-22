package com.gigforce.ambassador.user_rollment.kycdocs.bankaccount

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.ambassador.user_rollment.kycdocs.Data
import com.gigforce.ambassador.user_rollment.kycdocs.KycOcrResultModel
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationKycRepo
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UserBankAccountViewModel @Inject constructor(
    private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {
    private val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    var kycOcrResultModel: KycOcrResultModel? = null
    val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult
    val _kycVerifyResult = MutableLiveData<KycOcrResultModel>()
    val kycVerifyResult: LiveData<KycOcrResultModel> = _kycVerifyResult

    val _bankDetailedObject = MutableLiveData<BankDetailsDataModel>()
    val bankDetailedObject: LiveData<BankDetailsDataModel> = _bankDetailedObject


    fun getKycOcrResult(uid:String,type: String, subType: String, image: MultipartBody.Part) =
        viewModelScope.launch {
            try {
                _kycOcrResult.value =
                    verificationKycRepo.getVerificationOcrResult(uid,type, subType, image)
            } catch (e: Exception) {
                KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", kycOcrResultModel.toString())
        }

    fun getKycVerificationResult(uid:String,type: String, list: List<Data>) =
        viewModelScope.launch {
            try {
                _kycVerifyResult.value = verificationKycRepo.getKycVerification(uid,type, list)
            } catch (e: Exception) {
                KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", kycVerifyResult.toString())
        }

    fun getBankVerificationUpdation(uid:String) {
        verificationKycRepo.db.collection("Verification").document(uid)
            .addSnapshotListener { value, error ->
                value?.data?.let {
                    val doc = value.toObject(VerificationBaseModel::class.java)
                    doc?.bank_details?.let {
                        _bankDetailedObject.value = it
                    }
                }
            }
    }

    fun setVerificationStatusInDB(uid: String,status: Boolean) =
        viewModelScope.launch {
            try {
                verificationKycRepo.setVerifiedStatus(uid,status)
            } catch (e: Exception) {

            }
        }

    fun setVerificationStatusStringToBlank(uid: String) =
        viewModelScope.launch {
            try {
                verificationKycRepo.setVerificationStatusStringToBlank(uid)
            } catch (e: Exception) {

            }
        }
}