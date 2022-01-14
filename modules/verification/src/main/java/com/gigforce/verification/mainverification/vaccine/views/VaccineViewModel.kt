package com.gigforce.verification.mainverification.vaccine.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.VaccineFileUploadReqDM
import com.gigforce.common_ui.remote.verification.VaccineFileUploadResDM
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.IntermediatorRepo
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class VaccineViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo) :
    ViewModel() {
    private val _fileUploadLiveData = MutableLiveData<Lce<VaccineFileUploadResDM>>()
    val vaccineFileUploadResLiveData: LiveData<Lce<VaccineFileUploadResDM>> = _fileUploadLiveData

    private val _vaccineCertDetailsDM = MutableLiveData<Lce<VaccineCertDetailsDM>>()
    val vaccineCertDetailsDM : LiveData<Lce<VaccineCertDetailsDM>> = _vaccineCertDetailsDM

    fun uploadFile(vaccineReqDM: VaccineFileUploadReqDM, file: MultipartBody.Part) =
        viewModelScope.launch {
            try {
                _fileUploadLiveData.value = Lce.Loading
                _fileUploadLiveData.value = Lce.content(
                    verificationKycRepo.submitVaccinationCertificate(
                        vaccineReqDM,
                        file
                    )
                )
            } catch (e: Exception) {
                _fileUploadLiveData.value = Lce.error(e.toString())
            }

        }

    fun getVaccineDetailsData(vaccineId : String) = viewModelScope.launch{
        try {
            _vaccineCertDetailsDM.value = Lce.Loading
            _vaccineCertDetailsDM.value = Lce.content(verificationKycRepo.getVaccineDetailsData(vaccineId))
        }catch (e: Exception){
            _vaccineCertDetailsDM.value = Lce.error(e.toString())
        }
    }

}