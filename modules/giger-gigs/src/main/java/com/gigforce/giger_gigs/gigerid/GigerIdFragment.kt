package com.gigforce.giger_gigs.gigerid

//import com.gigforce.app.R
//import com.gigforce.app.core.base.BaseFragment

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.ViewModelProviderFactory
import com.gigforce.common_ui.utils.getScreenShot
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigOrder
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.*
import com.gigforce.giger_gigs.R
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_giger_id_fragment.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class GigerIdFragment : Fragment() {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(
                ViewModelGigerIDFragment(GigerIdRepository())
        )
    }
    private val viewModelGigerID: ViewModelGigerIDFragment by lazy {
        ViewModelProvider(this, viewModelFactory).get(ViewModelGigerIDFragment::class.java)
    }

    @Inject
    lateinit var navigation: INavigation

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_giger_id_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        StatusBarUtil.setColorNoTranslucent(
                requireActivity(),
                ResourcesCompat.getColor(
                        resources,
                        R.color.lipstick_2,
                        null
                )
        )
        initClicks()
        initObservers()
        viewModelGigerID.getProfileData()
    }

    fun iniFileSharing(it: String?) = runBlocking<Unit> {
        val bm = getScreenShot(cl_parent_giger_id)
        shareFile(
                imageToPDF(
                        storeImage(
                                bm,
                                StringConstants.GIGER_ID.value,
                                context?.filesDir?.absolutePath!!
                        ), it
                ), requireContext(), "*/*"
        )
    }

    private fun initObservers() {
        viewModelGigerID.observablePermGranted.observe(viewLifecycleOwner, Observer {
            iniFileSharing(it)
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
        viewModelGigerID.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it!!)
        })
        viewModelGigerID.observableUserProfileDataSuccess.observe(viewLifecycleOwner, Observer {
            viewModelGigerID.getGigDetails(arguments?.getString(com.gigforce.giger_gigs.GigPage2Fragment.INTENT_EXTRA_GIG_ID))

            viewModelGigerID.getProfilePicture(it?.profileAvatarName ?: "--")
            tv_giger_name_giger_id.text = it?.name ?: "--"

            val gigerPhoneNumber = if (!it?.contact.isNullOrEmpty()) {
                it!!.contact?.get(0)?.phone
            } else if (it?.loginMobile != null) {
                it.loginMobile
            } else
                "--"
            tv_contact_giger_id.text = gigerPhoneNumber
            viewModelGigerID.getURl()
        })
        viewModelGigerID.observableProfilePic.observe(viewLifecycleOwner, Observer {
            GlideApp.with(this.requireContext())
                    .load(it)
                    .apply(RequestOptions().circleCrop()).placeholder(R.drawable.ic_avatar_male)
                    .into(cv_profile_pic)
        })
        viewModelGigerID.observableGigDetails.observe(viewLifecycleOwner, Observer {
            initUi(it!!.gig, it.gigOrder)
        })
        viewModelGigerID.observableURLS.observe(viewLifecycleOwner, Observer {
            genQrCode(
                    "https://gigforce.in/users/" + viewModelGigerID.observableUserProfileDataSuccess.value?.id
            )
        })
    }

    private fun initUi(gig: Gig, gigOrder: GigOrder) {

        tv_designation_giger_id.text = gig.getGigTitle()
        tv_giger_location_giger_id.text =
            "${gigOrder.getGigOrderCity()}, ${gigOrder.getGigOrderState()}"
        tv_gig_since_giger_id.text =
            "${resources.getString(R.string.giger_since_giger_gigs)} ${
                parseTime(
                        "MMM yyyy",
                        gig.startDateTime.toDate()
                )
            }"
        if (!gig.getFullCompanyLogo().isNullOrBlank()) {
            if (gig.getFullCompanyLogo()!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                    .load(gig.getFullCompanyLogo())
                    .placeholder(getCircularProgressDrawable())
                    .into(iv_brand_logo_giger_id)
            } else {
                FirebaseStorage.getInstance()
                    .reference
                    .child(gig.getFullCompanyLogo()!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->

                        GlideApp.with(requireContext())
                            .load(fileUri)
                            .placeholder(getCircularProgressDrawable())
                            .into(iv_brand_logo_giger_id)
                    }
            }
        } else {
            val companyInitials = if (gig.getFullCompanyName().isNullOrBlank())
                "C"
            else
                gig.getFullCompanyName()!![0].toString().toUpperCase()
            val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            iv_brand_logo_giger_id.setImageDrawable(drawable)
        }
        tv_brand_name_giger_id.text = "@${gig.getFullCompanyName()}"
        tv_gig_id_giger_id.text = getString(R.string.activation_code_giger_gigs) + (gig.profile.activationCode ?: "NA")

        tv_gig_date_giger_id.text = parseTime("dd MMM yyyy", gigOrder.endDate.toDate())
        gig.assignedOn.let {

            tv_issued_date_giger_id.text =
                "${getString(R.string.issued_on_giger_gigs)} ${parseTime("dd MMM yyyy", it.toDate())}"
        }
        iv_share_giger_id.setOnClickListener {
            viewModelGigerID.showProgress(true)
            viewModelGigerID.checkForPermissionsAndInitSharing(true)
        }

        gig_act_code_copy_btn.setOnClickListener {
            val gigActivationCode = viewModelGigerID.currentGig?.gig?.profile?.activationCode ?: return@setOnClickListener

            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip: ClipData = ClipData.newPlainText("activation_code", gigActivationCode)
            clipboard?.setPrimaryClip(clip)

            showToast(getString(R.string.activation_code_copied_giger_gigs))
        }
    }

    private fun initClicks() {

        ic_close_giger_id.setOnClickListener {
            //popBackStack()
            navigation.popBackStack()
        }
    }


    fun genQrCode(url: String?) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
                url,
                BarcodeFormat.QR_CODE,
                512,
                512
        )
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
    suspend fun imageToPDF(imagePath: String, fileName: String?): File? {
        try {
            val document = Document()
            val dirPath = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/${fileName ?: ""}.pdf"
            PdfWriter.getInstance(document, FileOutputStream(dirPath)) //  Change pdf's name.
            document.open()
            val documentWidth: Float =
                document.pageSize.width - document.leftMargin() - document.rightMargin()
            val documentHeight: Float =
                document.pageSize.height - document.topMargin() - document.bottomMargin()
            val img: Image = Image.getInstance(imagePath)
            img.scaleToFit(documentWidth, documentHeight)
            img.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
            document.add(img)
            document.close()
            viewModelGigerID.showProgress(false)
            Toast.makeText(requireContext(), getString(R.string.file_downloaded_path_giger_gigs) + dirPath, Toast.LENGTH_LONG)
                .show()
            return File(dirPath)
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