package com.gigforce.verification.mainverification.vaccine.mainvaccine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.VaccineFileUploadResDM
import com.gigforce.common_ui.remote.verification.VaccineIdLabelReqDM
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM1
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.ConfigurationRepository
import com.gigforce.verification.mainverification.vaccine.models.VaccineConfigListDM
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class VaccineMainViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo) : ViewModel() {

    private val vaccineRepository = ConfigurationRepository()

    private val _vaccineConfigLiveData = MutableLiveData<Lce<VaccineConfigListDM>>()
    val vaccineConfigLiveData: LiveData<Lce<VaccineConfigListDM>> = _vaccineConfigLiveData

    private val _fileUploadLiveData = MutableLiveData<Lce<VaccineFileUploadResDM>>()
    val vaccineFileUploadResLiveData: LiveData<Lce<VaccineFileUploadResDM>> = _fileUploadLiveData


    init {
        getVaccineConfigData()
    }

    private fun getVaccineConfigData() = viewModelScope.launch {
        _vaccineConfigLiveData.value = Lce.loading()
        try {
            val data = vaccineRepository.getVaccineConfigData()
            _vaccineConfigLiveData.value = Lce.content(data)
        } catch (e: Exception) {
            _vaccineConfigLiveData.value = Lce.error(e.toString())
        }
    }

    fun uploadFile(vaccineReqDM: VaccineIdLabelReqDM, file: MultipartBody.Part) =
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

}