package com.gigforce.verification.mainverification.vaccine.component

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.utils.Lce
import com.gigforce.verification.mainverification.vaccine.DownloadFileService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FileDownloaded(val boolean: Boolean?=false)

@HiltViewModel
class VaccineCertDetailsComponentViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {

    private val _fileDownloaded = MutableLiveData<Lce<FileDownloaded>>()
    val fileDownloaded: LiveData<Lce<FileDownloaded>> = _fileDownloaded

    private var downloadAttachmentService: DownloadFileService =
        RetrofitFactory.createService(DownloadFileService::class.java)

    val gigforceDirectory: File by lazy {

        File(context.filesDir, "vaccine").apply {
            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    fun downloadFile(url: String) = viewModelScope.launch {
        try {

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
                    _fileDownloaded.value = Lce.error("File not able to download!!")
                }else{
                    _fileDownloaded.value = Lce.content(FileDownloaded(true))
                }
            } else {
                _fileDownloaded.value = Lce.error("File not able to download!!")
            }

        } catch (e: Exception) {
            Log.e("downloaddata","exception")
        }
    }

}