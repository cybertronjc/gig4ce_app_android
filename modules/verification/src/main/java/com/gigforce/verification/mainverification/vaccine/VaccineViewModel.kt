package com.gigforce.verification.mainverification.vaccine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.VaccineFileUploadReqDM
import com.gigforce.common_ui.remote.verification.VaccineFileUploadResDM
import com.gigforce.common_ui.viewdatamodels.BaseResponse
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM1
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import com.gigforce.verification.mainverification.vaccine.models.VaccineConfigListDM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class VaccineViewModel @Inject constructor(val logger: GigforceLogger, private val verificationKycRepo: VerificationKycRepo, private val intermediatorRepo: IntermediateVaccinationRepo) : ViewModel() {

    private val _vaccineConfigLiveData = MutableLiveData<Lce<VaccineConfigListDM>>()
    val vaccineConfigLiveData: LiveData<Lce<VaccineConfigListDM>> = _vaccineConfigLiveData

    private val _vaccineCertDetailsDM = MutableLiveData<Lce<VaccineCertDetailsDM>>()
    val vaccineCertDetailsDM : LiveData<Lce<VaccineCertDetailsDM>> = _vaccineCertDetailsDM

    private val _fileUploadLiveData = MutableLiveData<Lce<VaccineFileUploadResDM>>()
    val vaccineFileUploadResLiveData: LiveData<Lce<VaccineFileUploadResDM>> = _fileUploadLiveData

    private val _allVaccineLiveData = MutableLiveData<Lce<List<VaccineCertDetailsDM>>>()
    val allVaccineLiveData : LiveData<Lce<List<VaccineCertDetailsDM>>> = _allVaccineLiveData

    private val _confirmVaccineDetailLiveData = MutableLiveData<Lce<BaseResponse<Any>>>()
    val confirmVaccineDetailLiveData : LiveData<Lce<BaseResponse<Any>>> = _confirmVaccineDetailLiveData

    private val vaccineRepository = ConfigurationRepository()
    private val TAG = "Exception"

    var activeObserver : Boolean = true

    init {
        getVaccineConfigData()
    }

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
            _vaccineCertDetailsDM.value = Lce.content(intermediatorRepo.getVaccineDetailsData(vaccineId))
        }catch (e: Exception){
            _vaccineCertDetailsDM.value = Lce.error(e.toString())
        }
    }

    fun confirmVaccineData(vaccineId: String) = viewModelScope.launch {

        _confirmVaccineDetailLiveData.value = Lce.loading()

        try {
            _confirmVaccineDetailLiveData.value = Lce.content(verificationKycRepo.confirmVaccinationData(vaccineId))
        }catch (e:Exception){
            _confirmVaccineDetailLiveData.value = Lce.error(e.toString())
        }
    }


    private fun getVaccineConfigData() = viewModelScope.launch {
        _vaccineConfigLiveData.value = Lce.loading()
        try {
            val data = vaccineRepository.getVaccineConfigData()
            if(!data.list.isNullOrEmpty()){
                data.list.add(SimpleCardDVM1(id = null,label = "NOT VACCINATED", navPath = "verification/GetVaccinateFirstBS"))
            }
            _vaccineConfigLiveData.value = Lce.content(data)
        } catch (e: Exception) {
            _vaccineConfigLiveData.value = Lce.error(e.toString())
            logger.d(TAG, e.toString())
        }
    }

    fun getAllVaccineDetailData() = viewModelScope.launch {
        _allVaccineLiveData.value = Lce.loading()
        try {
            _allVaccineLiveData.value = Lce.content(intermediatorRepo.getAllVaccinationDataList())
        }catch (e:Exception){
            _allVaccineLiveData.value = Lce.error(e.toString())
        }
    }

}