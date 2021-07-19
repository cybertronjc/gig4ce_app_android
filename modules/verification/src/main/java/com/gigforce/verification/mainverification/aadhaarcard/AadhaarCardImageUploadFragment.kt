package com.gigforce.verification.mainverification.aadhaarcard

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardImageUploadFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.verification.gigerVerfication.aadharCard.AadharCardSides
import com.gigforce.verification.mainverification.Data
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import com.gigforce.verification.util.VerificationConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaeger.library.StatusBarUtil
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.aadhaar_card_image_upload_fragment.*
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
class AadhaarCardImageUploadFragment : Fragment(),
        VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        fun newInstance() = AadhaarCardImageUploadFragment()
        const val REQUEST_CODE_UPLOAD_AADHAR_IMAGE = 2333

        private const val REQUEST_CAPTURE_IMAGE = 1031
        private const val REQUEST_PICK_IMAGE = 1032

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 103
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: AadhaarCardImageUploadViewModel by viewModels()
    private lateinit var viewBinding: AadhaarCardImageUploadFragmentBinding
    private var aadharFrontImagePath: Uri? = null
    private var aadharBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null

    private fun activeLoader(activate: Boolean) {
        if (activate) {
            viewBinding.progressBar.visible()
            viewBinding.screenLoaderBar.visible()
            viewBinding.submitButton.isEnabled = false
        } else {
            viewBinding.progressBar.gone()
            viewBinding.screenLoaderBar.gone()
            viewBinding.submitButton.isEnabled = true
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadhaarCardImageUploadFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
//        return inflater.inflate(R.layout.aadhaar_card_image_upload_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent(savedInstanceState)
        setViews()
        observer()
        listeners()
        initWebview()
    }

    private fun initWebview() {
        context?.let {
            viewBinding.digilockerWebview.addJavascriptInterface(WebViewInterface(it), "Android")
            viewBinding.digilockerWebview.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                loadsImagesAutomatically = true
                domStorageEnabled = true
            }
//            viewBinding.digilockerWebview.loadUrl("http://dev.manchtech.com/esign.html?kycToken=oeDAPTj7KK9mWOHrRoNVKA%3D%3D")
            viewBinding.digilockerWebview.loadUrl("http://staging.gigforce.in/kyc/6yB48vGLTkTQYpaKXG4bapwcrtp2")
//            viewBinding.digilockerWebview.loadUrl("https://accounts.digitallocker.gov.in/signup/oauth_partner/%252Foauth2%252F1%252Fauthorize%253Fresponse_type%253Dcode%2526client_id%253D4A933F08%2526state%253DeyJ0eXBlIjoiRE9DVU1FTlQiLCJpZCI6MTcyMiwicGFyZW50UmVsSWQiOjM1OTU0LCJjbGllbnRSZXR1cm5VcmwiOiJodHRwOi8vc3RhZ2luZy5naWdmb3JjZS5pbi92ZXJpZnkva3ljLzZ5QjQ4dkdMVGtUUVlwYUtYRzRiYXB3Y3J0cDIvMzU5NTQifQ%25253D%25253D%2526orgid%253D002869%2526txn%253D60f4db02cf20bf7c4b587ef4oauth21626659586%2526hashkey%253D3a018e19317d39555fe1e7f8d6f3870cb72275c3bd1c5b58f0f1de408d3b7ea5%2526requst_pdf%253DY%2526enable_signup_link%253D1%2526disable_userpwd_login%253D1%2526aadhaar_only%253DY%2526app_name%253DTWFuY2hUZWNoIERldg%25253D%25253D%2526partner_name%253DTWFuY2ggVGVjaG5vbG9naWVzIFByaXZhdGUgTGltaXRlZA%25253D%25253D%2526authMode%253DO")
            viewBinding.digilockerWebview.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                    url?.let {
                        view.loadUrl(it)
                    }
                    return true
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    context?.let {
                        UtilMethods.showLongToast(it, error?.description.toString())
                    }
                }
            }

//            val rawHTML = "<!DOCTYPE >\n" +
//                    "<html>\n" +
//                    "  <head>\n" +
//                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
//                    "    <script type=\"text/javascript\">\n" +
//                    "           function init()\n" +
//                    "           {\n" +
//                    "              var testVal = 'Привет от Android Tools!';\n" +
//                    "              Android.sendData(\"working\");\n" +
//                    "           }\n" +
//                    "        </script>\n" +
//                    "  </head>\n" +
//                    "  <body>\n" +
//                    "    <div style=\"clear: both;height: 3px;\"> </div>\n" +
//                    "    <div>\n" +
//                    "      <input value=\"submit\" type=\"button\" name=\"submit\"\n" +
//                    "           id=\"btnSubmit\" onclick=\"javascript:return init();\" />\n" +
//                    "    </div>\n" +
//                    "  </body>\n" +
//                    "</html>"
//
//            viewBinding.digilockerWebview.loadData(rawHTML,"text/HTML", "UTF-8")
        }
    }

    class WebViewInterface {
        var context: Context
        var data: String? = null

        constructor(context: Context) {
            this.context = context
        }

        @JavascriptInterface
        fun sendData(data: String) {
            this.data = data
            Log.e("javascript", data)
            UtilMethods.showLongToast(context, data)
        }
    }

    var allNavigationList = ArrayList<String>()
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
        } ?: run {
            arguments?.let {
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
            }
        }

    }

    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            checkForPermissionElseShowCameraGalleryBottomSheet()
            //if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) openCameraAndGalleryOptionForFrontSideImage() else openCameraAndGalleryOptionForBackSideImage()
        })

        viewBinding.dateRlAadhar.setOnClickListener {
            dateOfBirthPicker.show()
        }

        viewBinding.submitButton.setOnClickListener {

            hideSoftKeyboard()
            if (toplayoutblock.isDocDontOptChecked()) {
                activity?.onBackPressed()
//                checkForNextDoc()
            } else {
                if (viewBinding.submitButton.tag?.toString().equals(CONFIRM_TAG)) {
//                    checkForNextDoc()
                    activity?.onBackPressed()
                } else {
                    if (viewBinding.aadharcardTil.editText?.text?.length != 12) {
                        MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.enter_valid_aadhar_no))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()
                        return@setOnClickListener
                    }
                    callKycVerificationApi()
                }
            }
        }

        viewBinding.toplayoutblock.whyweneedit.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.iconwhyweneed.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.appBarAadhar.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
    }

    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle = allNavigationList.slice(IntRange(1, allNavigationList.size - 1)).filter { it.length > 0 }
            }
            navigation.popBackStack()
            navigation.navigateTo(allNavigationList.get(0), bundleOf(VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle))

        }
    }

    val CONFIRM_TAG: String = "confirm"
    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            it?.let {
                if (it.status) {
                    if (!it.aadhaarNumber.isNullOrBlank() || !it.gender.isNullOrBlank() || !it.dateOfBirth.isNullOrBlank()) {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                                AppConstants.UPLOAD_SUCCESS,
                                "UPLOAD SUCCESSFUL",
                                "Information of Aadhar Card Captured Successfully."
                        )
                        if (!it.aadhaarNumber.isNullOrBlank())
                            viewBinding.aadharcardTil.editText?.setText(it.aadhaarNumber)
                        if (!it.name.isNullOrBlank())
                            viewBinding.nameTilAadhar.editText?.setText(it.name)
                        if (!it.dateOfBirth.isNullOrBlank())
                            viewBinding.dateOfBirthAadhar.text = it.dateOfBirth
                    } else {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                                AppConstants.UNABLE_TO_FETCH_DETAILS,
                                "UNABLE TO FETCH DETAILS",
                                "Enter your Aadhar details manually or try again to continue the verification process."
                        )
                    }
                } else {
                    viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "UNABLE TO FETCH DETAILS",
                            "Enter your Aadhar details manually or try again to continue the verification process."
                    )
                    showToast("Ocr status " + it.message)
                }
            }
        })

        viewModel.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            it?.let {
                if (it.status) {
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "Pending for Verify",
                            "Information of Aadhar Card Captured Successfully pending for verify!"
                    )
                    viewBinding.submitButton.tag = CONFIRM_TAG
                    viewBinding.toplayoutblock.setVerificationSuccessfulView("Aadhaar pending for verify", "Verifying")
                    viewBinding.submitButton.text = getString(R.string.submit)
                    viewBinding.toplayoutblock.disableImageClick()
                    viewBinding.toplayoutblock.hideOnVerifiedDocuments()
                } else
                    showToast("Verification status " + it.message)
            }
        })
        viewModel.getVerifiedStatus() //getting userHasVerified status
        viewModel.verifiedStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "Pending for Verify",
                            "Information of Aadhar Card Captured Successfully pending for verify!"
                    )
                    viewBinding.submitButton.tag = CONFIRM_TAG
                    viewBinding.toplayoutblock.setVerificationSuccessfulView("Aadhaar pending for verify", "Verifying")
                    viewBinding.toplayoutblock.disableImageClick()
                    viewBinding.toplayoutblock.hideOnVerifiedDocuments()
                }
            }
        })
    }


    private fun setViews() {
        //  ic_front   ic_back
        val frontUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.verification_doc_image))
                .appendPath(resources.getResourceTypeName(R.drawable.verification_doc_image))
                .appendPath(resources.getResourceEntryName(R.drawable.verification_doc_image))
                .build()
        val backUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.verification_doc_image))
                .appendPath(resources.getResourceTypeName(R.drawable.verification_doc_image))
                .appendPath(resources.getResourceEntryName(R.drawable.verification_doc_image))
                .build()
        val list = listOf(
                KYCImageModel(
                        getString(R.string.upload_aadhar_card_front_side_new),
                        imageIcon = frontUri,
                        imageUploaded = false
                ), KYCImageModel(text = getString(R.string.upload_aadhar_card_back_side_new), imageIcon = backUri, imageUploaded = false)
        )
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun callKycOcrApi(path: Uri) {

        var image: MultipartBody.Part? = null
        if (path != null) {
            val file = File(URI(path.toString()))
            Log.d("Register", "Nombre del archivo " + file.name)
            // create RequestBody instance from file
            val requestFile: RequestBody =
                    RequestBody.create(MediaType.parse("image/png"), file)
            // MultipartBody.Part is used to send also the actual file name
            image =
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
        }
        image?.let { viewModel.getKycOcrResult("aadhar", if (currentlyClickingImageOfSide == AadharCardSides.FRONT_SIDE) "front" else "back", it) }
    }

    private val dateOfBirthPicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    val newCal = Calendar.getInstance()
                    newCal.set(Calendar.YEAR, year)
                    newCal.set(Calendar.MONTH, month)
                    newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    viewBinding.dateOfBirthAadhar.text = DateHelper.getDateInDDMMYYYY(newCal.time)
                },
                1990,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            VerificationClickOrSelectImageBottomSheet.launch(
                    parentFragmentManager,
                    "Upload Aadhar Card",
                    this
            )
        else
            requestStoragePermission()
    }

    private fun requestStoragePermission() {

        requestPermissions(
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
        )
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
                "purpose",
                "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "aadhar_card_front.jpg")
        navigation.navigateToPhotoCrop(
                photoCropIntent,
                REQUEST_CODE_UPLOAD_AADHAR_IMAGE, requireContext(), this
        )
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
                "purpose",
                "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "aadhar_card_back.jpg")
        navigation.navigateToPhotoCrop(
                photoCropIntent,
                REQUEST_CODE_UPLOAD_AADHAR_IMAGE, requireContext(), this
        )
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {

                var allPermsGranted = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermsGranted = false
                        break
                    }
                }

                if (allPermsGranted)
                    VerificationClickOrSelectImageBottomSheet.launch(
                            parentFragmentManager,
                            "Upload Aadhar Card",
                            this
                    )
                else {
                    showToast("Please grant storage permission")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


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
            if (AadharCardSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                aadharFrontImagePath = imageUriResultCrop
                showFrontAadharCard(aadharFrontImagePath!!)
            } else if (AadharCardSides.BACK_SIDE == currentlyClickingImageOfSide) {
                aadharBackImagePath = imageUriResultCrop
                showBackAadharCard(aadharBackImagePath!!)
            }
            val baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                val bitmap = data.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

            }
        }


//                if (aadharDataCorrectCB.isChecked
//                    && aadharFrontImagePath != null
//                    && aadharBackImagePath != null
//                ) {
//                    enableSubmitButton()
//                } else {
//                    disableSubmitButton()
//                }
//
//                if (aadharFrontImagePath != null && aadharBackImagePath != null && aadharSubmitSliderBtn.isGone) {
//                    aadharSubmitSliderBtn.visible()
//                    aadharDataCorrectCB.visible()
//                }


    }
//    private fun showAadharImageAndInfoLayout() {
//        aadharBackImageHolder.visibility = View.VISIBLE
//        aadharFrontImageHolder.visibility = View.VISIBLE
//    }
//
//    private fun hideAadharImageAndInfoLayout() {
//        aadharBackImageHolder.visibility = View.GONE
//        aadharFrontImageHolder.visibility = View.GONE
//        aadharInfoLayout.visibility = View.GONE
//    }

    //    private fun enableSubmitButton() {
//        aadharSubmitSliderBtn.isEnabled = true
//
//        aadharSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        aadharSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }
//
//    private fun disableSubmitButton() {
//        aadharSubmitSliderBtn.isEnabled = false
//
//        aadharSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        aadharSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }
//
//    private fun showImageInfoLayout() {
//        aadharInfoLayout.visibility = View.VISIBLE
//    }
//
//
    private fun showFrontAadharCard(aadharFrontImagePath: Uri) {
//        aadharFrontImageHolder.makeEditLayoutVisible()
//        aadharFrontImageHolder.uploadImageLabel(getString(R.string.aadhar_card_front_image))
//
//        aadharFrontImageHolder.setImage(aadharFrontImagePath)
        viewBinding.toplayoutblock.setDocumentImage(0, aadharFrontImagePath)
        activeLoader(true)
        callKycOcrApi(aadharFrontImagePath)

    }

    private fun showBackAadharCard(aadharBackImagePath: Uri) {
//        aadharBackImageHolder.makeUploadLayoutVisible()
//        aadharBackImageHolder.uploadImageLabel(getString(R.string.aadhar_card_back_image))
//
//        aadharBackImageHolder .setImage(aadharBackImagePath)
        viewBinding.toplayoutblock.setDocumentImage(1, aadharBackImagePath)

    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                title = getString(R.string.why_do_we_need_this),
                content = getString(R.string.why_we_need_this_aadhar)
        )
    }

    private fun callKycVerificationApi() {
        var list = listOf(
                Data("name", viewBinding.nameTilAadhar.editText?.text.toString()),
                Data("no", viewBinding.aadharcardTil.editText?.text.toString()),
                Data("dob", viewBinding.dateOfBirthAadhar.text.toString())
        )
        activeLoader(true)
        viewModel.getKycVerificationResult("aadhar", list)
    }

    override fun onClickPictureThroughCameraClicked() {
        if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) {
            currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE
        } else {
            currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE
        }
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) {
            currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE
        } else {
            currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE
        }
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

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(resources, R.color.lipstick_2, null))
    }

}