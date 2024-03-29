package com.gigforce.verification.mainverification.vaccine.mainvaccine

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.VaccineFileUploadResDM
import com.gigforce.common_ui.remote.verification.VaccineIdLabelReqDM
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.DownloadFileService
import com.gigforce.verification.mainverification.vaccine.IntermediateVaccinationRepo
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

data class FileDownloaded(val boolean: Boolean?=false)

@HiltViewModel
class VaccineMainViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo, private val intermediatorRepo: IntermediateVaccinationRepo,@ApplicationContext val context: Context) : ViewModel() {

    private val _fileDownloaded = MutableLiveData<Lce<FileDownloaded>>()
    val fileDownloaded: LiveData<Lce<FileDownloaded>> = _fileDownloaded

    private val _vaccineConfigLiveData = MutableLiveData<Lce<List<VaccineCertDetailsDM>>>()
    val vaccineConfigLiveData: LiveData<Lce<List<VaccineCertDetailsDM>>> = _vaccineConfigLiveData

    private val _vaccineLiveData = MutableLiveData<Lce<VaccineCertDetailsDM>>()
    val vaccineLiveData: LiveData<Lce<VaccineCertDetailsDM>> = _vaccineLiveData

    private val _fileUploadLiveData = MutableLiveData<Lce<VaccineFileUploadResDM>>()
    val vaccineFileUploadResLiveData: LiveData<Lce<VaccineFileUploadResDM>> = _fileUploadLiveData

    @Inject
    lateinit var eventTracker: IEventTracker

    val gigforceDirectory: File by lazy {

        File(context.filesDir, "vaccine").apply {
            if (!this.exists()) {
                mkdirs()
            }
        }
    }
    private var downloadAttachmentService: DownloadFileService =
        RetrofitFactory.createService(DownloadFileService::class.java)


    fun getVaccineData(userIdToUse:String?) = viewModelScope.launch {
        _vaccineConfigLiveData.value = Lce.loading()
        try {
            val data = intermediatorRepo.getAllVaccinationDataList(userIdToUse)
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
                when (vaccineReqDM.vaccineLabel) {
                    "Dose 1" -> {
                        eventTracker.pushEvent(TrackingEventArgs("vaccine_certi1_uploaded", null))
                    }
                    "Dose 2" -> {
                        eventTracker.pushEvent(TrackingEventArgs("vaccine_certi2_uploaded", null))
                    }
                    "Booster Dose" -> {
                        eventTracker.pushEvent(TrackingEventArgs("vaccine_booster_uploaded", null))
                    }
                }
            } catch (e: Exception) {
                _fileUploadLiveData.value = Lce.error(e.toString())
            }

        }


    fun downloadFile(vaccineLabel: String, url: String) = viewModelScope.launch {
        try {
            _fileDownloaded.value = Lce.Loading
            val fullDownloadLink = FirebaseStorage.getInstance().reference.child(url).getDownloadUrlOrThrow().toString()

            val dirRef = gigforceDirectory
            if (!dirRef.exists()) dirRef.mkdirs()
            // download file from Server
            val response = downloadAttachmentService.downloadAttachment(fullDownloadLink)

            val fileName: String = FirebaseUtils.extractFilePath(fullDownloadLink)
            val fileRef = File(dirRef, fileName)

            if (response.isSuccessful) {
                val body = response.body()!!
                if (!FileUtils.writeResponseBodyToDisk(body, fileRef)) {
                    _fileDownloaded.value = Lce.error("File not able to download in storage!!")
                }else{
                    try {
                        MediaStoreApiHelpers.saveDocumentToDownloads(context, fileRef.toUri())
                        _fileDownloaded.value = Lce.content(FileDownloaded(true))
                        when (vaccineLabel) {
                            "Dose 1" -> {
                                eventTracker.pushEvent(TrackingEventArgs("vaccine_certi1_downloaded", null))
                            }
                            "Dose 2" -> {
                                eventTracker.pushEvent(TrackingEventArgs("vaccine_certi2_downloaded", null))
                            }
                            "Booster Dose" -> {
                                eventTracker.pushEvent(TrackingEventArgs("vaccine_booster_downloaded", null))
                            }
                        }
                    }catch (e:Exception){
                        _fileDownloaded.value = Lce.error("Error : File not able to download!!")
                    }
                }
            } else {
                _fileDownloaded.value = Lce.error("File not able to download through network!!")
            }

        } catch (e: Exception) {
            Log.e("downloaddata","exception")
        }
    }
}