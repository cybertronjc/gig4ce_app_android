package com.gigforce.verification.mainverification.compliance.components

import android.app.DownloadManager
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
import com.gigforce.common_ui.remote.verification.ComplianceDocumentDetailDM
import com.gigforce.core.IViewHolder
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.databinding.ComplianceDocumentDetailComponentBinding
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
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
        startDocumentDownload(currentData.path)
    }

    private fun startDocumentDownload(
        url: String
    ) {
        try {
            val filePathName = FirebaseUtils.extractFilePath(url)

            val downloadRequest = DownloadManager.Request(Uri.parse(url)).run {
                setTitle(filePathName)
                setDescription("Offer Letter")
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    filePathName
                )
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
            }

            val downloadManager = context
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(downloadRequest)

            ToastHandler.showToast(
                context,
                "Saving file in Downloads,check notification...",
                Toast.LENGTH_LONG
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}