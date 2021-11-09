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

    val _bankDetailedObject = MutableLiveData<BankDetailsDataModel>()
    val bankDetailedObject: LiveData<BankDetailsDataModel> = _bankDetailedObject


    fun getKycOcrResult(type: String, subType: String, image: MultipartBody.Part, uid: String) =
        viewModelScope.launch {
            try {
                _kycOcrResult.value =
                    verificationKycRepo.getVerificationOcrResult(type, uid, subType, image)
            } catch (e: Exception) {
                _kycOcrResult.value = KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", kycOcrResultModel.toString())
        }

    fun getKycVerificationResult(type: String, list: List<Data>, uid: String) =
        viewModelScope.launch {
            try {
                _kycVerifyResult.value = verificationKycRepo.getKycVerification(type, list, uid)
            } catch (e: Exception) {
                _kycVerifyResult.value = KycOcrResultModel(status = false, message = e.message)
            }
            Log.d("result", kycVerifyResult.toString())
        }

    fun getBankVerificationUpdation() {
        verificationKycRepo.db.collection("Verification").document(verificationKycRepo.getUID())
            .addSnapshotListener { value, error ->
                value?.data?.let {
                    val doc = value.toObject(VerificationBaseModel::class.java)
                    doc?.bank_details?.let {
                        _bankDetailedObject.value = it
                    }
                }
            }
    }

    fun getBankDetailsStatus(uid: String) {
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

    fun setVerificationStatusInDB(status: Boolean, uid: String) =
        viewModelScope.launch {
            try {
                verificationKycRepo.setVerifiedStatus(status, uid)
            } catch (e: Exception) {

            }
        }

    fun setVerificationStatusStringToBlank() =
        viewModelScope.launch {
            try {
                verificationKycRepo.setVerificationStatusStringToBlank()
            } catch (e: Exception) {

            }
        }
}