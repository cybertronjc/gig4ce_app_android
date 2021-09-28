package com.gigforce.common_ui.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import com.gigforce.common_ui.R
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.repository.BannerCardRepository
import com.gigforce.core.base.BaseActivity
import com.gigforce.core.datamodels.AccessLogResponse
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.acitivity_doc_viewer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.*

class DocViewerActivity : BaseActivity() {
    private var pdfView: WebView? = null
    private var progress: ProgressBar? = null

    //var pageTitle: String? = null
    private var win: Window? = null
    private val removePdfTopIcon =
        "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"
    var purposeExtra = ""
    var bundle: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_doc_viewer)
        pdfView = findViewById(R.id.webview)
        progress = findViewById(R.id.pb_doc)
        val stringExtra = intent.getStringExtra(StringConstants.DOC_URL.value)
        purposeExtra = intent.getStringExtra(StringConstants.DOC_PURPOSE.value) ?: ""
        bundle = intent.extras
        //pageTitle = intent.getStringExtra(StringConstants.WEB_TITLE.value)
        showPdfFile(
            stringExtra,
            stringExtra.contains(".jpg") || stringExtra.contains(".png"),
            stringExtra.contains(".pdf")
        )
        makeToolbarVisible(
            stringExtra.contains(".jpg") || stringExtra.contains(".png") || stringExtra.contains(
                ".pdf"
            ), purposeExtra
        )
        if(purposeExtra == "banner"){
            bannerEntered()
        }
        setListeners(stringExtra)
    }
    var accessLogResponse : AccessLogResponse?=null
    private fun bannerEntered() {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            var url = bundle?.getString("apiUrl")
            var source = bundle?.getString("source")?:""
            val bannerName = bundle?.getString("bannerName")?:""
            val id = bundle?.getString("bannerId")?:""
            url?.let {
                accessLogResponse = bannerCardRepo.createLogs(
                    it,
                    FirebaseAuth.getInstance().currentUser?.uid!!,
                    source,
                    bannerName,
                    id
                )
            }
        }
    }


    private fun makeToolbarVisible(isImageOrPdf: Boolean, purpose: String) {
//       if (isImageOrPdf && !purpose.equals("OFFER_LETTER")){
//           toolbar_doc.gone()
//           acceptLayout.gone()
//       } else {
//           changeStatusBarColor()
//           acceptLayout.visible()
        if (purpose == "OFFER_LETTER" && isImageOrPdf) {
            toolbarTitle.text = getString(R.string.offer_letter_common_ui)
            toolbar_doc.visible()
            changeStatusBarColor()
            toolbarDownload.visible()
            acceptLayout.gone()
        } else if (purpose == "TERMS" && !isImageOrPdf) {
            toolbarTitle.text = getString(R.string.terms_common_ui)
            toolbar_doc.visible()
            changeStatusBarColor()
            toolbarDownload.gone()
            acceptLayout.visible()
        }else if(purpose == "banner") {
            var title = if(bundle?.getString("title").isNullOrBlank()){
                if(bundle?.getString("defaultDocTitle").isNullOrBlank()){
                    "Back to Gigforce"
                }else{
                    bundle?.getString("defaultDocTitle")
                }
            }else{bundle?.getString("title")}

            toolbarTitle.text = title
            toolbar_doc.visible()
            changeStatusBarColor()
            toolbarDownload.gone()
            acceptLayout.gone()
        }
        else {
            toolbar_doc.gone()
            toolbarDownload.gone()
            acceptLayout.gone()
        }
        // }

    }

    private fun setListeners(url: String) {

        toolbarBack.setOnClickListener {
            onBackPressed()
        }
        //toolbarTitle.text = pageTitle
        accept.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        toolbarDownload.setOnClickListener {
            if (url.isNotEmpty()) {
                showDownloadStartedDialog()
                downloadFile(
                    this,
                    url.substring(url.lastIndexOf('/') + 1),
                    ".pdf",
                    DIRECTORY_DOWNLOADS,
                    url
                )
            }

        }
    }

    private fun changeStatusBarColor() {
        win = this.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        win?.statusBarColor = resources.getColor(R.color.status_bar_pink)
    }

    private fun showPdfFile(imageString: String?, isImage: Boolean, isPdf: Boolean) {
        showProgress()
        pdfView!!.invalidate()
        pdfView!!.settings.javaScriptEnabled = true
        pdfView!!.settings.setSupportZoom(true)
        pdfView!!.settings.builtInZoomControls = true
        pdfView!!.settings.loadWithOverviewMode = true
        pdfView!!.settings.useWideViewPort = true

        pdfView!!.loadUrl(
            if (isPdf)
                "https://docs.google.com/gview?embedded=true&url=${
                    URLEncoder.encode(
                        imageString,
                        "UTF-8"
                    )
                }"
            else imageString
        )
        pdfView!!.webViewClient = object : WebViewClient() {
            var checkOnPageStartedCalled = false
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                checkOnPageStartedCalled = true
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (checkOnPageStartedCalled) {
                    pdfView!!.loadUrl(removePdfTopIcon)
                    hideProgress()
                } else {
                    showPdfFile(imageString, isImage, isPdf)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Toast.makeText(
                    this@DocViewerActivity,
                    getString(R.string.error_loading_page_common_ui),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun showProgress() {
        progress!!.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progress!!.visibility = View.GONE
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

    private fun showDownloadStartedDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alert_common_ui))
            .setMessage(getString(R.string.download_started_common_ui))
            .setPositiveButton(getString(R.string.okay_common_ui)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private var bannerCardRepo = BannerCardRepository()
    override fun onBackPressed() {
        if (purposeExtra == "banner") {
            bannerUpdateRequest()

        }
        super.onBackPressed()
    }

    private fun bannerUpdateRequest() {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            bundle?.let { bundle->
                accessLogResponse?.let {
                    var url = bundle.getString("apiUrl")
                    if(!url.isNullOrEmpty() && !it._id.isNullOrEmpty()) {
                        bannerCardRepo.updateLogs(
                            url,
                            FirebaseAuth.getInstance().currentUser?.uid!!,
                            it._id!!,
                            "home"
                        )
                    }
                }

            }

        }
    }

}