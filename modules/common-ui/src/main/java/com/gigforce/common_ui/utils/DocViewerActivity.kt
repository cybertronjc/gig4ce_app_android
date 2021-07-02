package com.gigforce.common_ui.utils

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.common_ui.R
import com.gigforce.common_ui.StringConstants
import java.net.URLEncoder

class DocViewerActivity : AppCompatActivity() {
    private var pdfView: WebView? = null
    private var progress: ProgressBar? = null
    private val removePdfTopIcon =
        "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_doc_viewer)
        pdfView = findViewById(R.id.webview)
        progress = findViewById(R.id.pb_doc)
        val stringExtra = intent.getStringExtra(StringConstants.DOC_URL.value)
        showPdfFile(stringExtra, stringExtra.contains(".jpg") || stringExtra.contains(".png"), stringExtra.contains(".pdf"));
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