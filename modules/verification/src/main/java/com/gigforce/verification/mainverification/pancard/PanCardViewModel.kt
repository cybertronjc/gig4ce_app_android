package com.gigforce.verification.mainverification.pancard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.ambassador.user_rollment.kycdocs.Data
import com.gigforce.core.datamodels.verification.PanCardDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.common_ui.remote.verification.KycOcrResultModel
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class PanCardViewModel @Inject constructor(
    private val verificationKycRepo: VerificationKycRepo
) : ViewModel() {

//    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult
    val _kycVerifyResult = MutableLiveData<KycOcrResultModel>()
    val kycVerifyResult: LiveData<KycOcrResultModel> = _kycVerifyResult

    val _verifiedStatus = MutableLiveData<PanCardDataModel>()
    val verifiedStatus: LiveData<PanCardDataModel> = _verifiedStatus

    fun getKycOcrResult(type: String, subType: String, image: MultipartBody.Part, uid: String) =
            viewModelScope.launch {
                try {
                    _kycOcrResult.value = verificationKycRepo.getVerificationOcrResult(type, uid, subType, image)
                } catch (e: Exception) {
                    _kycOcrResult.value = KycOcrResultModel(status = false, message = e.message)
                }
                Log.d("result", _kycOcrResult.toString())
            }

    fun getKycVerificationResult(type: String, list: List<Data>, uid: String) =
            viewModelScope.launch {
                try {
                    _kycVerifyResult.value = verificationKycRepo.getKycVerification(type, list, uid)
                } catch (e: Exception) {
                    Log.d("result", e.message.toString())
                    _kycVerifyResult.value = KycOcrResultModel(status = false, message = e.message)
                }
                Log.d("result", _kycVerifyResult.toString())
            }

    fun getVerifiedStatus(uid: String) {
        verificationKycRepo.db.collection("Verification").document(uid)
                .addSnapshotListener { value, error ->
                    value?.data?.let {
                        val doc = value.toObject(VerificationBaseModel::class.java)
                        doc?.pan_card?.let {
                            _verifiedStatus.value = it
                        }
                    }
                }
    }
}