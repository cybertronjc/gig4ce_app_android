package com.gigforce.verification.mainverification.compliance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.compliance.models.ComplianceDocDetailsDM
import com.gigforce.verification.mainverification.compliance.models.DataListItem
import com.gigforce.verification.mainverification.vaccine.IntermediateVaccinationRepo
import com.gigforce.verification.mainverification.vaccine.mainvaccine.FileDownloaded
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
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

    fun getComplianceData(userIdToUse:String?) = viewModelScope.launch {
        _complianceLiveData.value = Lce.loading()
        try {
            val data = arrayListOf<ComplianceDocDetailsDM>()
            data.add(
                ComplianceDocDetailsDM(
                    type = "offer_letter",
                    name = "OFFER LETTER",
                    value = "",
                    path = null,
                    data = arrayListOf(
                        DataListItem(
                            "Gigforce Offer Letter",
                            "http://www.africau.edu/images/default/sample.pdf"
                        ), DataListItem(
                            "Delhivery Offer Letter",
                            "http://www.africau.edu/images/default/sample.pdf"
                        )
                    )
                )
            )
            data.add(
                ComplianceDocDetailsDM(
                    type = "uan",
                    name = "UAN",
                    value = "4754343543543",
                    path = null,
                    data = null
                )
            )
            data.add(
                ComplianceDocDetailsDM(
                    type = "esic",
                    name = "ESIC",
                    value = "4754343543543",
                    path = null,
                    data = null
                )
            )
            data.add(
                ComplianceDocDetailsDM(
                    type = "pf",
                    name = "PF",
                    value = "4754343543543",
                    path = null,
                    data = null
                )
            )



            _complianceLiveData.value = Lce.content(data)
//            val data = intermediatorRepo.getAllVaccinationDataList(userIdToUse)
//            _complianceLiveData.value = Lce.content(data)
        } catch (e: Exception) {
            _complianceLiveData.value = Lce.error(e.toString())
        }
    }
}