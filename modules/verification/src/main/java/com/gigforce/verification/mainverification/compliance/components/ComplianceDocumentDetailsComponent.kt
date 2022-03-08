package com.gigforce.verification.mainverification.compliance.components

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.remote.verification.ComplianceDocDetailsDM
import com.gigforce.common_ui.remote.verification.ComplianceDocumentDetailDM
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.utils.Lce
import com.gigforce.verification.databinding.ComplianceDocumentDetailComponentBinding
import com.gigforce.verification.databinding.ComplianceNumberDetailsComponentBinding
import com.gigforce.verification.mainverification.vaccine.DownloadFileService
import com.gigforce.verification.mainverification.vaccine.mainvaccine.FileDownloaded
import com.google.firebase.storage.FirebaseStorage
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ComplianceDocumentDetailsComponent (context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder, View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    private var viewBinding: ComplianceDocumentDetailComponentBinding
    //Data
    private lateinit var currentData: ComplianceDocumentDetailDM

    init {
        viewBinding = ComplianceDocumentDetailComponentBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewBinding.downloadIcon.setOnClickListener(this)
    }


    override fun bind(data: Any?) {
        if (data is ComplianceDocumentDetailDM) {
            currentData = data
            data.name.let {
                if (it?.isNotBlank() == true){
                    viewBinding.docTitle.setText(data.name)
                }
            }
            data.jobProfile.let {
                if (it?.isNotBlank() == true){
                    viewBinding.docSubvalue.setText(Html.fromHtml("Job Profile: <b>${data.jobProfile}</b>"))
                }
            }
            data.dateOfGeneration.let {
                if (it.isNotBlank()){
                    viewBinding.docDateofgeneration.setText(Html.fromHtml("Date of Generation: <b>${data.dateOfGeneration}</b>"))
                }
            }
        }
    }

    override fun onClick(p0: View?) {
            //download file
        Toast.makeText(context, "Download started, please check notification", Toast.LENGTH_SHORT).show()
        downloadFile(context, currentData.path.substring(currentData.path.lastIndexOf('/') + 1),
            ".pdf",
            Environment.DIRECTORY_DOWNLOADS,
            currentData.path)
    }

    private fun downloadFile(
        context: Context,
        fileName: String,
        fileExtension: String,
        destination: String,
        url: String
    ) {
        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, destination, fileName + fileExtension)
        request.setTitle(fileName)
        request.setMimeType("application/pdf")
        downloadManager.enqueue(request)
    }
}