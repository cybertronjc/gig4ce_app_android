package com.gigforce.verification.mainverification.bankaccount

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.Data
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.common_ui.remote.verification.KycOcrResultModel
import com.gigforce.common_ui.remote.verification.UserConsentResponse
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class BankAccountViewModel @Inject constructor(
//    private val iBuildConfigVM: IBuildConfigVM
    private val verificationKycRepo: VerificationKycRepo
) : ViewModel() {
//    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    var kycOcrResultModel: KycOcrResultModel? = null
    private val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult
    private val _kycVerifyResult = MutableLiveData<KycOcrResultModel>()
    val kycVerifyResult: LiveData<KycOcrResultModel> = _kycVerifyResult

    private val _bankDetailedObject = MutableLiveData<BankDetailsDataModel>()
    val bankDetailedObject: LiveData<BankDetailsDataModel> = _bankDetailedObject

    private val _userConsentModel = MutableLiveData<Lce<UserConsentResponse>>()
    val userConsentModel : LiveData<Lce<UserConsentResponse>> = _userConsentModel
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
    var lastRequest : Boolean? = null
    fun setVerificationStatusInDB(status: Boolean, uid: String) =
        viewModelScope.launch {
            lastRequest = status
            _userConsentModel.value = Lce.Loading
            try {
                val responseData = verificationKycRepo.setVerifiedStatus(status, uid)
                responseData.optionSelected = lastRequest.toString()
                _userConsentModel.value = Lce.content(responseData)
            } catch (e: Exception) {
                _userConsentModel.value = Lce.error(e.toString())
            }
        }

    fun setUserAknowledge(uid: String) =
        viewModelScope.launch {
            _userConsentModel.value = Lce.Loading
            try {
                _userConsentModel.value = Lce.content(verificationKycRepo.setUserAknowledge())
            } catch (e: Exception) {
                _userConsentModel.value = Lce.error(e.toString())
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