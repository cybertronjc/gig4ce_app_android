package com.gigforce.verification.mainverification.bankaccount

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.StructuredName.PREFIX
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.BankAccountFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.verification.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import com.gigforce.verification.mainverification.Data
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import com.gigforce.verification.mainverification.pancard.PanCardFragment
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bank_account_fragment.*
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BankAccountFragment : Fragment(),
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        fun newInstance() = BankAccountFragment()
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
        const val INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT = "user_came_from_amb_screen"
        private const val REQUEST_CAPTURE_IMAGE = 1012
        private const val REQUEST_PICK_IMAGE = 1013

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewModel: BankAccountViewModel
    private var didUserCameFromAmbassadorScreen = false
    private var clickedImagePath: Uri? = null
    private lateinit var viewBinding: BankAccountFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = BankAccountFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BankAccountViewModel::class.java)
        setViews()
        observer()
        listeners()
    }

    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            it.let {
                showToast("Ocr status " + it.status)
            }
        })

        viewModel.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            it.let {
                showToast("Verification " + it.status)
            }
        })
    }

    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            //showCameraAndGalleryOption()
            VerificationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                "Upload Bank Passbook",
                this
            )
        })
        submit_button_bank.setOnClickListener {
            callKycVerificationApi()
        }

        viewBinding.toplayoutblock.querytext.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.imageView7.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        appBarBank.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
    }

    private fun setViews() {
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_front))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_front))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_front))
            .build()
        val list =
            listOf(KYCImageModel(getString(R.string.upload_bank_account_new), frontUri, false))
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun callKycOcrApi(path: Uri) {

        var image: MultipartBody.Part? = null
        if (path != null) {
            val file = File(URI(path.toString()))
            Log.d("Register", "Nombre del archivo " + file.name)
            // create RequestBody instance from file
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            // MultipartBody.Part is used to send also the actual file name
            image =
                MultipartBody.Part.createFormData("imagenPerfil", file.name, requestFile)
        }
        image?.let { viewModel.getKycOcrResult("bank", "", it) }
    }


    private fun showCameraAndGalleryOption() {
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
            "purpose",
            "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "pan_card.jpg")
        navigation.navigateToPhotoCrop(
            photoCropIntent,
            REQUEST_CODE_CAPTURE_BANK_PHOTO, requireContext(), this
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
                val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
                if (outputFileUri != null) {
                    startCrop(outputFileUri)
                } else {
                    showToast(getString(R.string.issue_in_cap_image))
                }
            } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
                val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
                Log.d("ImageUri", imageUriResultCrop.toString())
                clickedImagePath = imageUriResultCrop
                showPassbookInfoCard(clickedImagePath!!)
                val baos = ByteArrayOutputStream()
                if (imageUriResultCrop == null) {
                    val bitmap = data.data as Bitmap
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

                }
            }

//            if (resultCode == Activity.RESULT_OK) {
//                clickedImagePath =
//                    data?.getParcelableExtra("uri")
//                showPassbookInfoCard(clickedImagePath!!)
//
////                if (bankDetailsDataConfirmationCB.isChecked)
////                    enableSubmitButton()
////
////                if (clickedImagePath != null && passbookSubmitSliderBtn.isGone) {
////                    bankDetailsDataConfirmationCB.visible()
////                    passbookSubmitSliderBtn.visible()
////                }
//
//            }
        }
    }

//    private fun disableSubmitButton() {
//        passbookSubmitSliderBtn.isEnabled = false
//
//        passbookSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        passbookSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }
//
//    private fun showPassbookImageLayout() {
//        passbookImageHolder.visibility = View.VISIBLE
//    }
//
//    private fun showPassbookInfoLayout() {
//        passbookInfoLayout.visibility = View.VISIBLE
//    }
//
//    private fun hidePassbookImageAndInfoLayout() {
//        passbookImageHolder.visibility = View.GONE
//        passbookInfoLayout.visibility = View.GONE
//    }
//
//    private fun enableSubmitButton() {
//        passbookSubmitSliderBtn.isEnabled = true
//
//        passbookSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        passbookSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }
    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this),
            content = getString(R.string.why_do_we_need_this_bank)
        )
    }

    private fun callKycVerificationApi() {
        var list = listOf(
            Data("name", account_number_itl.editText?.text.toString()),
            Data("no", bank_name_til.editText?.text.toString()),
            Data("ifsccode", ifsc_code.editText?.text.toString()),
            Data("holdername", account_holder_name.editText?.text.toString())
        )
        viewModel.getKycVerificationResult("bank", list)
    }

    private fun showPassbookInfoCard(bankInfoPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(0, bankInfoPath)
        callKycOcrApi(bankInfoPath)
    }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }
    private fun startCrop(uri: Uri): Unit {
        Log.v("Start Crop", "started")
        //can use this for a new name every time
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"
        val uCrop: UCrop = UCrop.of(
            uri,
            Uri.fromFile(File(requireContext().cacheDir, imageFileName + EXTENSION))
        )
        val resultIntent: Intent = Intent()
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())
        uCrop.start(requireContext(), this)
    }

    private fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
//        options.setMaxBitmapSize(1000)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(false)
        options.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.topBarDark, null))
        options.setToolbarColor(ResourcesCompat.getColor(resources, R.color.topBarDark, null))
        options.setToolbarTitle(getString(R.string.crop_and_rotate))
        return options
    }


}