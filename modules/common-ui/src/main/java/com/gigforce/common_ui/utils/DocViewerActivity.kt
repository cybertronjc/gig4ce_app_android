package com.gigforce.common_ui.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.common_ui.R
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.acitivity_doc_viewer.*
import java.net.URLEncoder

class DocViewerActivity : AppCompatActivity() {
    private var pdfView: WebView? = null
    private var progress: ProgressBar? = null
    var pageTitle: String? = null
    private var win: Window? = null
    private val removePdfTopIcon =
        "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_doc_viewer)
        pdfView = findViewById(R.id.webview)
        progress = findViewById(R.id.pb_doc)
        val stringExtra = intent.getStringExtra(StringConstants.DOC_URL.value)
        pageTitle = intent.getStringExtra(StringConstants.WEB_TITLE.value)
        showPdfFile(stringExtra, stringExtra.contains(".jpg") || stringExtra.contains(".png"), stringExtra.contains(".pdf"));
        makeToolbarVisible(stringExtra.contains(".jpg") || stringExtra.contains(".png"), stringExtra.contains(".pdf"))
        setListeners()
    }

    private fun makeToolbarVisible(isImage: Boolean, isPdf: Boolean) {
       if (isImage || isPdf){
           toolbar_doc.gone()
           acceptLayout.gone()
       } else {
           changeStatusBarColor()
           toolbar_doc.visible()
           acceptLayout.visible()
       }

    }

    private fun setListeners() {

        toolbarBack.setOnClickListener {
            onBackPressed()
        }
        toolbarTitle.text = pageTitle
        accept.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
    private fun changeStatusBarColor() {
        win = this.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_pink))
    }

    private fun showPdfFile(imageString: String?, isImage: Boolean, isPdf: Boolean) {
        showProgress()
        pdfView!!.invalidate()
        pdfView!!.settings.javaScriptEnabled = true
        pdfView!!.settings.setSupportZoom(true)
        pdfView!!.settings.builtInZoomControls = true;
        pdfView!!.settings.loadWithOverviewMode = true;
        pdfView!!.settings.useWideViewPort = true;

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
        }
    }

    fun showProgress() {
        progress!!.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progress!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}