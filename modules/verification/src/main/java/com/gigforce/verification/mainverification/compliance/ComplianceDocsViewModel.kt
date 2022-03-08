package com.gigforce.verification.mainverification.compliance

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.ComplianceDataModel
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.common_ui.remote.verification.ComplianceDocDetailsDM
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.verification.mainverification.vaccine.DownloadFileService
import com.gigforce.verification.mainverification.vaccine.IntermediateVaccinationRepo
import com.gigforce.verification.mainverification.vaccine.mainvaccine.FileDownloaded
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ComplianceDocsViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo, private val intermediatorRepo: IntermediateVaccinationRepo, @ApplicationContext val context: Context) : ViewModel() {
    private val _complianceLiveData = MutableLiveData<Lce<ComplianceDataModel>>()
    val complianceLiveData: LiveData<Lce<ComplianceDataModel>> = _complianceLiveData


    fun getComplianceData() = viewModelScope.launch {
        _complianceLiveData.value = Lce.loading()
        try {
            _complianceLiveData.value = Lce.content(verificationKycRepo.getComplianceData())
        } catch (e: Exception) {
            _complianceLiveData.value = Lce.error(e.toString())
        }
    }

}