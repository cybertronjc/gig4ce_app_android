package com.gigforce.verification.mainverification.compliance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.common_ui.remote.verification.ComplianceDocDetailsDM
import com.gigforce.verification.mainverification.vaccine.IntermediateVaccinationRepo
import com.gigforce.verification.mainverification.vaccine.mainvaccine.FileDownloaded
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplianceDocsViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo, private val intermediatorRepo: IntermediateVaccinationRepo, @ApplicationContext val context: Context) : ViewModel() {
    private val _fileDownloaded = MutableLiveData<Lce<FileDownloaded>>()
    val fileDownloaded: LiveData<Lce<FileDownloaded>> = _fileDownloaded

    private val _complianceLiveData = MutableLiveData<Lce<List<ComplianceDocDetailsDM>>>()
    val complianceLiveData: LiveData<Lce<List<ComplianceDocDetailsDM>>> = _complianceLiveData

    fun getComplianceData() = viewModelScope.launch {
        _complianceLiveData.value = Lce.loading()
        try {
            _complianceLiveData.value = Lce.content(verificationKycRepo.getComplianceData())
        } catch (e: Exception) {
            _complianceLiveData.value = Lce.error(e.toString())
        }
    }
}