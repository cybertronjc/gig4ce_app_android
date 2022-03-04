package com.gigforce.verification.mainverification.compliance

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.IntermediateVaccinationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ComplianceDocsViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo, private val intermediatorRepo: IntermediateVaccinationRepo, @ApplicationContext val context: Context) : ViewModel() {

}