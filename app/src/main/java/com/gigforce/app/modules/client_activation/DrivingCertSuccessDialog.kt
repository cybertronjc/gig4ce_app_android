package com.gigforce.app.modules.client_activation

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.PermissionUtils
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_dialog_driving_certificate_success.*

class DrivingCertSuccessDialog : DialogFragment() {

    private lateinit var mDocURL: String
    private lateinit var callbacks: DrivingCertSuccessDialogCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.layout_dialog_driving_certificate_success,
            container,
            false
        )
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(
                    R.dimen.size_32
                ), ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initClicks()
    }

    private fun initClicks() {
//        tv_okay_driving_cert_success.setOnClickListener {
//            dismiss()
//            callbacks.onClickOkay()
//        }
        PushDownAnim.setPushDownAnimTo(tv_okay_driving_cert_success)
                .setOnClickListener(View.OnClickListener {
                    dismiss()
                    callbacks.onClickOkay()

//                    if (PermissionUtils.checkForPermissionFragment(
//                                    this,
//                                    PermissionUtils.reqCodePerm,
//                                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                    ) {
//
////                        downloadCertificate(mDocURL)
//                    }
                })

    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PermissionUtils.reqCodePerm && PermissionUtils.permissionsGrantedCheck(
//                grantResults!!
//            )
//        ) {
//            tv_okay_driving_cert_success.performClick()
//        } else {
//            Toast.makeText(
//                requireContext(),
//                getString(R.string.perm_not_granted),
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }

    fun setCallbacks(callbacks: DrivingCertSuccessDialogCallbacks) {
        this.callbacks = callbacks
    }

    interface DrivingCertSuccessDialogCallbacks {
        fun onClickOkay()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mDocURL = it.getString(StringConstants.DOC_URL.value,"") ?: ""

        }

        arguments?.let {
            mDocURL = it.getString(StringConstants.DOC_URL.value,"") ?: ""

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.DOC_URL.value, mDocURL)


    }

    private fun downloadCertificate(url: String) {
//        if (File(
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                        "DrivingCertificate.pdf"
//                ).exists()
//        ) {
//            Toast.makeText(requireContext(), getString(R.string.downloaded_certificate_exists), Toast.LENGTH_LONG).show()
//            return
//        }

//        val url = URLEncoder.encode(downloadUrl, "UTF-8")
        val downloadmanager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setTitle("Driving Certificate")
        request.setDescription("Downloading Driving Certificate")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setVisibleInDownloadsUi(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOCUMENTS,
                "DrivingCertificate.pdf"
            )
        } else {
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "DrivingCertificate.pdf"
            )
        }
        if (downloadmanager != null) {
            try {
                Toast.makeText(
                    context,
                    "Your Driving Certificate is Downloading",
                    Toast.LENGTH_SHORT
                ).show()
                downloadmanager.enqueue(request)
            } catch (e: Exception) {
                Toast.makeText(context, "NetWork Error. Please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(context, "Network Error. Please try again", Toast.LENGTH_SHORT).show()
        }
        val progressBarDialog = ProgressDialog(context)
        progressBarDialog.setTitle("Downloading Driving Certificate, Please Wait...")
        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressBarDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, "OK"
        ) { dialog: DialogInterface?, whichButton: Int ->
            dismiss()
            callbacks.onClickOkay()
        }
        progressBarDialog.progress = 0
        Thread {
            var downloading = true
            while (downloading) {
                val q = DownloadManager.Query()
                q.setFilterById() //filter by id which you have receieved when reqesting download from download manager
                var cursor: Cursor? = null
                if (downloadmanager != null) {
                    cursor = downloadmanager.query(q)
                }
                cursor?.moveToFirst()
                var bytes_downloaded = 0
                if (cursor != null) {
                    bytes_downloaded = cursor.getInt(
                        cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                }
                var bytes_total = 0
                if (cursor != null) {
                    bytes_total =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                }
                if (cursor != null && cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val dl_progress = (bytes_downloaded * 100L / bytes_total).toInt()
                requireActivity().runOnUiThread { progressBarDialog.progress = dl_progress }
                cursor!!.close()
            }
        }.start()
        progressBarDialog.show()
    }


}