package com.gigforce.verification.mainverification.pancard

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.remote.verification.Data
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.*
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.datamodels.verification.PanCardDataModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.utils.VerificationValidations
import com.gigforce.verification.R
import com.gigforce.verification.databinding.PanCardFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.verification.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.verification.mainverification.OLDStateHolder
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import com.gigforce.verification.util.VerificationConstants
import com.gigforce.verification.util.VerificationEvents
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jaeger.library.StatusBarUtil
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


enum class VerificationScreenStatus {
    OCR_COMPLETED,
    VERIFIED,
    STARTED_VERIFYING,
    FAILED,
    COMPLETED,
    DEFAULT,
    STARTED
}

@AndroidEntryPoint
class PanCardFragment : Fragment(),
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener,
    IOnBackPressedOverride {

    companion object {
        fun newInstance() = PanCardFragment()
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path"
        const val INTENT_EXTRA_PAN = "pan"
        private const val REQUEST_CAPTURE_IMAGE = 1001
        private const val REQUEST_PICK_IMAGE = 1002

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 104
    }
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var buildConfig: IBuildConfig
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private var clickedImagePath: Uri? = null
    private val viewModel: PanCardViewModel by viewModels()
    private lateinit var viewBinding: PanCardFragmentBinding
    var verificationScreenStatus = VerificationScreenStatus.DEFAULT
    var ocrOrVerificationRquested = false
    private var userId: String? = null
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }
    private var userIdToUse: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = PanCardFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreAndCommonUtilInterface.saveDataBoolean(
            com.gigforce.common_ui.StringConstants.PAN_CARD_SP.value,
            false
        )
        getDataFromIntent(savedInstanceState)
        initviews()
        initializeImageViews()
        listeners()
        observer()
    }

    private fun initviews() {
        viewBinding.toplayoutblock.setIdonthaveDocContent(
            resources.getString(R.string.no_doc_title_pan_veri),
            resources.getString(R.string.no_doc_subtitle_pan_veri)
        )
        userIdToUse = if (userId != null) {
            userId
        }else{
            user?.uid
        }
    }

    var allNavigationList = ArrayList<String>()
    var intentBundle: Bundle? = null
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
            userId = it.getString(AppConstants.INTENT_EXTRA_UID) ?: return@let
        } ?: run {
            arguments?.let {
                FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
                intentBundle = it
                userId = it.getString(AppConstants.INTENT_EXTRA_UID) ?: return@let
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
        outState.putString(AppConstants.INTENT_EXTRA_UID, userId)
    }

    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            verificationScreenStatus = VerificationScreenStatus.OCR_COMPLETED
            activeLoader(false)
            it?.let {
                if (it.status) {
                    if (!it.panNumber.isNullOrBlank()) {

                        if (VerificationValidations.isPanCardValid(it.panNumber!!)) {
                            var map = mapOf(
                                "PAN number" to it.panNumber.toString()
                            )
                            eventTracker.pushEvent(
                                TrackingEventArgs(
                                    eventName = VerificationEvents.PAN_OCR_SUCCESS,
                                    props = map
                                )
                            )

                            viewBinding.panTil.editText?.setText(it.panNumber)
                            viewBinding.toplayoutblock.uploadStatusLayout(
                                AppConstants.UPLOAD_SUCCESS,
                                getString(R.string.upload_success_veri),
                                getString(R.string.pan_info_captured_veri)
                            )
                        } else {
                            eventTracker.pushEvent(
                                TrackingEventArgs(
                                    eventName = VerificationEvents.PAN_OCR_SUCCESS,
                                    props = mapOf("Data Captured" to false)
                                )
                            )
                            viewBinding.toplayoutblock.uploadStatusLayout(
                                AppConstants.UNABLE_TO_FETCH_DETAILS,
                                getString(R.string.unable_to_fetch_info_veri),
                                getString(R.string.enter_pan_details_manually_veri)
                            )
                        }

//                        if (!it.name.isNullOrBlank())
//                            viewBinding.nameTil.editText?.setText(it.name)
//                        if (!it.dateOfBirth.isNullOrBlank()) {
//                            if (it.dateOfBirth.contains("/") || it.dateOfBirth.contains("-")) {
//                                viewBinding.dateOfBirth.text = it.dateOfBirth
//                                viewBinding.dobLabel.visible()
//                            }
//                        }

                    } else {
                        eventTracker.pushEvent(
                            TrackingEventArgs(
                                eventName = VerificationEvents.PAN_OCR_FAILED,
                                props = null
                            )
                        )

                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            getString(R.string.unable_to_fetch_info_veri),
                            getString(R.string.enter_pan_details_manually_veri)
                        )
                    }

                } else {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.PAN_OCR_FAILED,
                            props = null
                        )
                    )
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UNABLE_TO_FETCH_DETAILS,
                        getString(R.string.unable_to_fetch_info_veri),
                        getString(R.string.enter_pan_details_manually_veri)
                    )
                    showToast("Ocr status " + it.message)
                }
            }
            ocrOrVerificationRquested = false
        })

        viewModel.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            ocrOrVerificationRquested = false
            if (!it.status){
                activeLoader(false)
                it.message?.let { it1 -> showToast(it1) }
            }
        })

        viewModel.getVerifiedStatus(userIdToUse.toString())
        viewModel.verifiedStatus.observe(viewLifecycleOwner, Observer {
            if (!ocrOrVerificationRquested) {
                viewBinding.screenLoaderBar.gone()
                it?.let {

                    if (it.verified) {

                        var props = HashMap<String, Any>()
                        props.put("PAN verified", true)
                        eventTracker.setUserProperty(props)

                        verificationScreenStatus = VerificationScreenStatus.VERIFIED
                        verifiedStatusViews(it)
                        viewBinding.belowLayout.visible()
                        setAlreadyfilledData(it, false)
                        viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                    } else {
                        checkforStatusAndVerified(it)
                    }
                }
            }
        })
    }

    var anyDataEntered = false

    inner class ValidationTextWatcher :
        TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            context?.let { cxt ->
                if (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.FAILED || verificationScreenStatus == VerificationScreenStatus.OCR_COMPLETED) {
                    text?.let {
                        if (viewBinding.nameTil.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.panTil.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.fatherNameTil.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.dateOfBirth.text.toString()
                                .isNullOrBlank()
                        ) {
                            viewBinding.submitButton.text = getString(R.string.skip_veri)
                            anyDataEntered = false
                        } else {
                            viewBinding.submitButton.text = getString(R.string.submit_veri)
                            anyDataEntered = true
                        }
                    }
                }
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

    }

    var oldStateHolder = OLDStateHolder("")
    private fun listeners() {
        viewBinding.toplayoutblock.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { p1, b1 ->
            if (b1) {
                oldStateHolder.submitButtonCta = viewBinding.submitButton.text.toString()
                viewBinding.submitButton.text = getString(R.string.skip_veri)
                viewBinding.belowLayout.gone()
            } else {
                viewBinding.submitButton.text = oldStateHolder.submitButtonCta
                viewBinding.belowLayout.visible()
            }

        })
        viewBinding.nameTil.editText?.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        viewBinding.panTil.editText?.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        viewBinding.fatherNameTil.editText?.addTextChangedListener(
            ValidationTextWatcher(
            )
        )
        viewBinding.dateOfBirth.addTextChangedListener(ValidationTextWatcher())

        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            checkForPermissionElseShowCameraGalleryBottomSheet()
            //launchSelectImageSourceDialog()
        })

        viewBinding.dateRl.setOnClickListener {
            dateOfBirthPicker.show()
        }

        viewBinding.submitButton.setOnClickListener {
            hideSoftKeyboard()
            if (viewBinding.toplayoutblock.isDocDontOptChecked() || !anyDataEntered || verificationScreenStatus == VerificationScreenStatus.VERIFIED || verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
                checkForNextDoc()
            } else {
                if (verificationScreenStatus == VerificationScreenStatus.FAILED) {
                    viewBinding.toplayoutblock.statusDialogLayoutvisibilityGone()
                }
                val panCardNo =
                    viewBinding.panTil.editText?.text.toString().toUpperCase(Locale.getDefault())
                if (clickedImagePath == null || clickedImagePath.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.upload_pan_image_first_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                if (!VerificationValidations.isPanCardValid(panCardNo)) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.enter_valid_pan_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                callKycVerificationApi()
            }
        }

        viewBinding.toplayoutblock.whyweneedit.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.iconwhyweneed.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.appBarPan.apply {
            setBackButtonListener(View.OnClickListener {
//                navigation.popBackStack()
                activity?.onBackPressed()
            })
        }

    }

    private fun setAlreadyfilledData(
        panCardDataModel: PanCardDataModel,
        enableFields: Boolean
    ): ArrayList<KYCImageModel> {

        viewBinding.nameTil.editText?.setText(panCardDataModel.name)

        viewBinding.panTil.editText?.setText(panCardDataModel.panCardNo)

        viewBinding.fatherNameTil.editText?.setText(panCardDataModel.fathername)

        panCardDataModel.dob?.let {

            viewBinding.dateOfBirth.text = panCardDataModel.dob

            viewBinding.dobLabel.visible()

        }

        val list = ArrayList<KYCImageModel>()

        panCardDataModel.panCardImagePath?.let {

            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_pan_card_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
                clickedImagePath = Uri.parse(it)
            }

        }

        viewBinding.toplayoutblock.setImageViewPager(list)
        viewBinding.nameTil.editText?.isEnabled = enableFields
        viewBinding.panTil.editText?.isEnabled = enableFields
        viewBinding.fatherNameTil.editText?.isEnabled = enableFields
        viewBinding.dateRl.isEnabled = enableFields
        viewBinding.dateOfBirth.isEnabled = enableFields
        if (enableFields) {
            viewBinding.textView10.visible()
        } else {
            viewBinding.textView10.gone()
        }
        return list
    }

    var manuallyRequestBackpress = false
    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
//            manuallyRequestBackpress = true
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle =
                    allNavigationList.slice(IntRange(1, allNavigationList.size - 1))
                        .filter { it.length > 0 }
            }
            navigation.popBackStack()

            intentBundle?.putStringArrayList(
                com.gigforce.common_ui.StringConstants.NAVIGATION_STRING_ARRAY.value,
                java.util.ArrayList(navigationsForBundle)
            )
            navigation.navigateTo(
                allNavigationList.get(0), intentBundle
            )
//            navigation.navigateTo(
//                allNavigationList.get(0),
//                bundleOf(VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle,if(FROM_CLIENT_ACTIVATON) StringConstants.FROM_CLIENT_ACTIVATON.value to true else StringConstants.FROM_CLIENT_ACTIVATON.value to false)
//            )

        }
    }

    private fun initializeImageViews() {
        viewBinding.toplayoutblock.showUploadHere()
        //ic_pan_illustration
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.verification_doc_image))
            .appendPath(resources.getResourceTypeName(R.drawable.verification_doc_image))
            .appendPath(resources.getResourceEntryName(R.drawable.verification_doc_image))
            .build()
        val list = listOf(
            KYCImageModel(
                text = getString(R.string.upload_pan_card_new_veri),
                imageIcon = frontUri,
                imageUploaded = false
            )
        )
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            VerificationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                getString(R.string.upload_pan_card_veri),
                this
            )
        else
            requestStoragePermission()
    }

    private fun requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )

        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }


    }

    private fun hasStoragePermissions(): Boolean {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else {

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
        image?.let {
            eventTracker.pushEvent(TrackingEventArgs(VerificationEvents.PAN_OCR_STARTED, null))
            viewModel.getKycOcrResult("pan", "dsd", it, userIdToUse.toString())
        }
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
                viewBinding.dateOfBirth.text = DateHelper.getDateInDDMMYYYY(newCal.time)
                viewBinding.dobLabel.visible()
            },
            1990,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun launchSelectImageSourceDialog() {
        val photoCropIntent = Intent()
        photoCropIntent.putExtra("purpose", "verification")
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "pan_card.jpg")
        navigation.navigateToPhotoCrop(
            photoCropIntent,
            AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE, requireContext(), this
        )
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
                        getString(R.string.upload_pan_card_veri),
                        this
                    )
                else {
                    showToast(getString(R.string.grant_storage_permission_veri))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
//                startCrop(outputFileUri)
                startCropImage(outputFileUri)
                Log.d("image", outputFileUri.toString())
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
            Log.d("ImageUri", imageUriResultCrop.toString())
            clickedImagePath = imageUriResultCrop
            showPanInfoCard(clickedImagePath!!)
        } else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? =
                Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
            Log.d("ImageUri", imageUriResultCrop.toString())
            clickedImagePath = imageUriResultCrop
            showPanInfoCard(clickedImagePath)
        }
    }

    private fun startCropImage(imageUri: Uri): Unit {
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)

    }

    private fun callKycVerificationApi() {
        var list = listOf(
//            Data("name", viewBinding.nameTil.editText?.text.toString()),
            Data("no", viewBinding.panTil.editText?.text.toString())
//            Data("fathername", viewBinding.fatherNameTil.editText?.text.toString()),
//            Data("dob", viewBinding.dateOfBirth.text.toString())
        )
        var map = mapOf(
            "PAN number" to viewBinding.panTil.editText?.text.toString()
        )
        eventTracker.pushEvent(
            TrackingEventArgs(
                eventName = VerificationEvents.PAN_DETAIL_SUBMITTED,
                props = map
            )
        )
        activeLoader(true)
        viewModel.getKycVerificationResult("pan", list, userIdToUse.toString())

    }

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

    private fun showPanInfoCard(panInfoPath: Uri?) {
        panInfoPath?.let{
            viewBinding.toplayoutblock.setDocumentImage(0, it)
            //call ocr api
            activeLoader(true)
            callKycOcrApi(it)
        }

    }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_do_we_need_this_pan_veri)
        )
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
        val size = getImageDimensions(uri)
        uCrop.withAspectRatio(size.width.toFloat(), size.height.toFloat())
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())
        uCrop.start(requireContext(), this)
    }

    private fun getImageDimensions(uri: Uri): Size {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        return Size(imageWidth, imageHeight)
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
        options.setToolbarTitle(getString(R.string.crop_and_rotate_veri))
        return options
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
    }


    private fun verifiedStatusViews(panCardDataModel: PanCardDataModel?) {
        viewBinding.toplayoutblock.viewChangeOnVerified()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.uploadStatusLayout(
            AppConstants.UPLOAD_SUCCESS,
            getString(R.string.verification_completed_veri),
            getString(R.string.pan_verified_successfully_veri)
        )
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = getString(R.string.next_camel_veri)
        viewBinding.submitButton.isEnabled = true
        viewBinding.progressBar.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(getString(R.string.pan_verified_veri))

        var list = ArrayList<KYCImageModel>()
        panCardDataModel?.panCardImagePath?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_pan_card_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        viewBinding.toplayoutblock.setImageViewPager(list)

    }

    fun getDBImageUrl(imagePath: String): String? {
        if (imagePath.isNotBlank()) {
            try {
                var modifiedString = imagePath
                if (!imagePath.startsWith("/"))
                    modifiedString = "/$imagePath"
                return buildConfig.getStorageBaseUrl() + modifiedString
            } catch (egetDBImageUrl: Exception) {
                return null
            }
        }
        return null
    }

    private val WAITING_TIME: Long = 1000 * 3
    private fun checkforStatusAndVerified(panCardDataModel: PanCardDataModel) {
        panCardDataModel.status?.let {
            when (it) {
                "started" -> {
                    verificationScreenStatus = VerificationScreenStatus.STARTED_VERIFYING
                    startedStatusViews(panCardDataModel)
                    Handler().postDelayed({
                        try {
                            if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
                                viewBinding.screenLoaderBar.gone()
                                verifiedStatusViews(null)
                                viewBinding.toplayoutblock.uploadStatusLayout(
                                    AppConstants.UNABLE_TO_FETCH_DETAILS,
                                    getString(R.string.verification_progress_veri),
                                    getString(R.string.doc_verified_soon_veri)
                                )
                                viewBinding.toplayoutblock.setVerificationSuccessfulView("", "")
                                viewBinding.belowLayout.visible()
                                setAlreadyfilledData(panCardDataModel, false)
                                viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                            }
                        } catch (e: Exception) {

                        }
                    }, WAITING_TIME)
                    viewBinding.belowLayout.visible()
                    setAlreadyfilledData(panCardDataModel, false)
                    viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                }
                "failed" -> {
                    verificationScreenStatus = VerificationScreenStatus.FAILED
                    resetInitializeViews()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.DETAILS_MISMATCH,
                        getString(R.string.verification_failed_veri),
                        getString(R.string.details_incorrect_veri)
                    )
                    var listData = setAlreadyfilledData(panCardDataModel, true)
                    if (listData.isEmpty()) {
                        initializeImageViews()
                    } else {
                        //single if showing error
                    }
                    viewBinding.toplayoutblock.enableImageClick()//keep this line in end only
                }
                "" -> {
                    verificationScreenStatus = VerificationScreenStatus.DEFAULT
                    resetInitializeViews()
                    viewBinding.toplayoutblock.enableImageClick()//keep this line in end only
                }
                else -> "unmatched status"
            }
        }
    }

    private fun resetInitializeViews() {
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = getString(R.string.skip_veri)
        viewBinding.submitButton.isEnabled = true
        viewBinding.belowLayout.visible()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.pan_card_veri),
            getString(R.string.you_need_to_upload_veri)
        )
        initializeImageViews()
        viewBinding.toplayoutblock.resetAllViews()
    }

    private fun startedStatusViews(panCardDataModel: PanCardDataModel) {
        viewBinding.toplayoutblock.viewChangeOnStarted()
        viewBinding.screenLoaderBar.visible()
        viewBinding.submitButton.gone()
        viewBinding.progressBar.gone()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.pan_pending_for_verification_veri),
            getString(R.string.verifying_veri)
        )
        var list = ArrayList<KYCImageModel>()
        panCardDataModel.panCardImagePath?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_pan_card_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            if (!manuallyRequestBackpress) { // || viewBinding.toplayoutblock.isDocDontOptChecked() || (!anyDataEntered && (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.FAILED))
                val navFragmentsData = activity as NavFragmentsData
                navFragmentsData.setData(
                    bundleOf(
                        StringConstants.BACK_PRESSED.value to true
                    )
                )
            }
        }
        return false
    }

}