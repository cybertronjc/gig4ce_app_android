package com.gigforce.verification.mainverification.aadhardetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.State
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.IAadhaarDetailsRepository
import com.gigforce.verification.mainverification.KycOcrResultModel
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class AadharDetailInfoViewModel @Inject constructor(private val aadharDetailsRepo: IAadhaarDetailsRepository, private val iBuildConfigVM: IBuildConfigVM) : ViewModel() {

    val statesResult: MutableLiveData<MutableList<State>> = MutableLiveData<MutableList<State>>()
    val _kycOcrResult = MutableLiveData<KycOcrResultModel>()
    val kycOcrResult: LiveData<KycOcrResultModel> = _kycOcrResult

    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    fun getStates() = viewModelScope.launch {
        try {
            val states = aadharDetailsRepo.getStatesFromDb()
            statesResult.postValue(states)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getKycOcrResult(type: String, subType: String, image: MultipartBody.Part) =
            viewModelScope.launch {
                try {
                    _kycOcrResult.value = verificationKycRepo.getVerificationOcrResult(type, subType, image)
                } catch (e: Exception) {
                    _kycOcrResult.value = KycOcrResultModel(status = false, message = e.message)
                }
                Log.d("result", _kycOcrResult.toString())
            }
}