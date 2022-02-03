package com.gigforce.verification.mainverification.vaccine.component

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.verification.mainverification.vaccine.DownloadFileService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VaccineCertDetailsComponentViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
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
//                    throw Exception("Unable to save downloaded chat attachment")
                }
            } else {
//                throw Exception("Unable to download attachment")
            }

        } catch (e: Exception) {
            Log.e("downloaddata","exception")
        }
    }

}