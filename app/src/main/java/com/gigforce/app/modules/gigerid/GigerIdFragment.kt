package com.gigforce.app.modules.gigerid

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.GigPageFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.layout_giger_id_fragment.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


class GigerIdFragment : BaseFragment() {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ViewModelGigerIDFragment(GigerIdRepository()))
    }
    private val viewModelGigerID: ViewModelGigerIDFragment by lazy {
        ViewModelProvider(this, viewModelFactory).get(ViewModelGigerIDFragment::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_giger_id_fragment, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        genQrCode()
        initClicks()

        initObservers()
        viewModelGigerID.getProfileData()
        viewModelGigerID.getGigDetails(arguments?.getString(GigPageFragment.INTENT_EXTRA_GIG_ID))
    }

    fun iniFileSharing() = runBlocking<Unit> {
        shareFile(
            imageToPDF(
                storeImage(
                    getBitmapFromView(
                        sv_giger_id,
                        sv_giger_id.getChildAt(0).height,
                        sv_giger_id.getChildAt(0).width
                    ),
                    StringConstants.GIGER_ID.value, context?.filesDir?.absolutePath!!
                )
            )!!, requireContext(), "*/*"
        )
    }

    private fun initObservers() {
        viewModelGigerID.observablePermGranted.observe(viewLifecycleOwner, Observer {
            iniFileSharing()
        })
        viewModelGigerID.observablePermResultsNotGranted.observe(
            viewLifecycleOwner,
            Observer {
                checkForRequiredPermissions()
            })
        viewModelGigerID.observableProgress.observe(
            viewLifecycleOwner,
            Observer {
                pb_giger_id.visibility = it!!
            })
        viewModelGigerID.observableUserProfileDataFailure.observe(viewLifecycleOwner, Observer {
            showToast(it!!)
        })
        viewModelGigerID.observableUserProfileDataSuccess.observe(viewLifecycleOwner, Observer {
            viewModelGigerID.getProfilePicture(it?.profileAvatarName ?: "--")
            tv_giger_name_giger_id.text = it?.name ?: ""
            tv_giger_location_giger_id.text =
                "${it?.address?.current?.city} , ${it?.address?.current?.state}"
            tv_contact_giger_id.text = it?.contact?.get(0)?.phone ?: "--"
            tv_email_giger_id.text = it?.contact?.get(0)?.email ?: "--"
        })
        viewModelGigerID.observableProfilePic.observe(viewLifecycleOwner, Observer {
            GlideApp.with(this.requireContext())
                .load(it)
                .apply(RequestOptions().circleCrop())
                .into(cv_profile_pic)
        })
        viewModelGigerID.observableGigDetails.observe(viewLifecycleOwner, Observer {
            initUi(it!!)
        })
    }

    private fun initUi(gig: Gig) {
        tv_designation_giger_id.text = gig.title
        tv_gig_since_giger_id.text =
            "${resources.getString(R.string.giger_since)} ${parseTime(
                "MMM yyyy",
                gig?.startDateTime?.toDate()
            )}"
        GlideApp.with(this.requireContext())
            .load(gig.companyLogo)
            .apply(RequestOptions().circleCrop())
            .into(iv_brand_logo_giger_id)
        tv_brand_name_giger_id.text = "@${gig.companyName}"
        tv_gig_id_giger_id.text = "${getString(R.string.gig_id)} ${gig.gigId}"
        gig.startDateTime?.let {
            tv_gig_date_giger_id.text = parseTime("dd MMM yyyy", it.toDate())
            tv_issued_date_giger_id.text =
                "${getString(R.string.issued_on)} ${parseTime("dd MMM yyyy", it.toDate())}"
        }
    }

    private fun initClicks() {
        iv_share_giger_id.setOnClickListener {
            viewModelGigerID.showProgress(true)
            viewModelGigerID.checkForPermissionsAndInitSharing(checkForRequiredPermissions())
        }
    }


    fun genQrCode() {
        val content =
            "Jai Shree Ram"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        iv_qr_code_giger_id.setImageBitmap(bitmap)
    }


    @Throws(FileNotFoundException::class)
    suspend fun imageToPDF(imagePath: String): File? {
        try {
            val document = Document()
            val dirPath = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/${System.currentTimeMillis()}.pdf"
            PdfWriter.getInstance(document, FileOutputStream(dirPath)) //  Change pdf's name.
            document.open()
            val img: Image = Image.getInstance(imagePath)
            img.scaleToFit(document.pageSize.width, document.pageSize.height)
            img.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
            document.add(img)
            document.close()
            viewModelGigerID.showProgress(false)
            Toast.makeText(requireContext(), "File Downloaded At Path $dirPath", Toast.LENGTH_LONG)
                .show()
            return File(dirPath);
        } catch (ignored: Exception) {
            return null
        }
    }

    private fun checkForRequiredPermissions(): Boolean {
        return PermissionUtils.checkForPermissionFragment(
            this,
            PermissionUtils.reqCodePerm,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModelGigerID.onActivityResultCalled(requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModelGigerID.checkIfPermGranted(requestCode, grantResults)
    }


}