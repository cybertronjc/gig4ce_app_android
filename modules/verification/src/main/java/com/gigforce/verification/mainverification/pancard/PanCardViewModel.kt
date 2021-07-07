package com.gigforce.verification.mainverification.pancard

import androidx.lifecycle.ViewModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.verification.mainverification.VerificationKycRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PanCardViewModel @Inject constructor(
    private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {

    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)


}