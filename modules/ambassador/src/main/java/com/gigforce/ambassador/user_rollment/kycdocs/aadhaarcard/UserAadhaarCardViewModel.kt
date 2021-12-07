package com.gigforce.ambassador.user_rollment.kycdocs.aadhaarcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationKycRepo
import com.gigforce.core.datamodels.verification.AadharCardDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserAadhaarCardViewModel @Inject constructor(
    private val verificationKycRepo: VerificationKycRepo
) : ViewModel() {
//    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    val _verifiedStatus = MutableLiveData<AadharCardDataModel>()
    val verifiedStatus: LiveData<AadharCardDataModel> = _verifiedStatus


    fun getVerifiedStatus(uid:String) {
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