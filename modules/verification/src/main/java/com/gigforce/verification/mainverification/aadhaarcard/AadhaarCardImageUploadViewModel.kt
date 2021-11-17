package com.gigforce.verification.mainverification.aadhaarcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.AadharCardDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AadhaarCardImageUploadViewModel @Inject constructor(
        private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {
    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    val _verifiedStatus = MutableLiveData<AadharCardDataModel>()
    val verifiedStatus: LiveData<AadharCardDataModel> = _verifiedStatus


    fun getVerifiedStatus(uid: String) {
        verificationKycRepo.db.collection("Verification").document(uid)
                .addSnapshotListener { value, error ->
                    value?.data?.let {
                        val doc = value.toObject(VerificationBaseModel::class.java)
                        doc?.aadhar_card?.let {
                            _verifiedStatus.value = it
                        }
                    }
                }
    }
}