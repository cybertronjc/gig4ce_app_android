package com.gigforce.verification.mainverification.character_certificate

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.remote.verification.CharacterCertificateResponse
import com.gigforce.common_ui.remote.verification.ComplianceDataModel
import com.gigforce.common_ui.remote.verification.VaccineFileUploadResDM
import com.gigforce.common_ui.remote.verification.VaccineIdLabelReqDM
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.datamodels.verification.CharacterCertificateDataModel
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.DownloadFileService
import com.gigforce.verification.mainverification.vaccine.IntermediateVaccinationRepo
import com.gigforce.verification.mainverification.vaccine.mainvaccine.FileDownloaded
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CharacterCertificateViewModel @Inject constructor(private val verificationKycRepo: VerificationKycRepo, private val intermediatorRepo: IntermediateVaccinationRepo, @ApplicationContext val context: Context) : ViewModel() {
    private val _characterLiveData = MutableLiveData<Lce<CharacterCertificateResponse>>()
    val characterLiveData: LiveData<Lce<CharacterCertificateResponse>> = _characterLiveData

    private val _fileDownloaded = MutableLiveData<Lce<FileDownloaded>>()
    val fileDownloaded: LiveData<Lce<FileDownloaded>> = _fileDownloaded

    private val _fileUploadLiveData = MutableLiveData<Lce<VaccineFileUploadResDM>>()
    val characterFileUploadResLiveData: LiveData<Lce<VaccineFileUploadResDM>> = _fileUploadLiveData

    val gigforceDirectory: File by lazy {

        File(context.filesDir, "character").apply {
            if (!this.exists()) {
                mkdirs()
            }
        }
    }
    private var downloadAttachmentService: DownloadFileService =
        RetrofitFactory.createService(DownloadFileService::class.java)

    fun getCharacterData() = viewModelScope.launch {
        _characterLiveData.value = Lce.loading()
        try {
            _characterLiveData.value = Lce.content(verificationKycRepo.getCharacterCertificate())
        } catch (e: Exception) {
            _characterLiveData.value = Lce.error(e.toString())
        }
    }

    fun uploadCharacterCertificate(file: MultipartBody.Part, updatedBy: RequestBody, updatedAt: RequestBody) =
        viewModelScope.launch {
            try {
                _fileUploadLiveData.value = Lce.Loading
                _fileUploadLiveData.value = Lce.content(
                    verificationKycRepo.submitCharacterCertificate(
                        file,
                        updatedBy,
                        updatedAt
                    )
                )
            } catch (e: Exception) {
                _fileUploadLiveData.value = Lce.error(e.toString())
            }

        }

    fun downloadFile(url: String) = viewModelScope.launch {
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