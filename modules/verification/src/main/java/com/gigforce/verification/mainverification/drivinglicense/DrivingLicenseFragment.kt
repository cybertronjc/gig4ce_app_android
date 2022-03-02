package com.gigforce.verification.mainverification.drivinglicense

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
import com.gigforce.core.datamodels.verification.DrivingLicenseDataModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.R
import com.gigforce.verification.databinding.DrivingLicenseFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.verification.gigerVerfication.drivingLicense.DrivingLicenseSides
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
import java.io.ByteArrayOutputStream
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
    DEFAULT
}

@AndroidEntryPoint
class DrivingLicenseFragment : Fragment(),
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener,
    IOnBackPressedOverride {
    companion object {
        fun newInstance() = DrivingLicenseFragment()
        const val REQUEST_CODE_UPLOAD_DL = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_FRONT = "front_image"
        const val INTENT_EXTRA_CLICKED_IMAGE_BACK = "back_image"
        const val INTENT_EXTRA_STATE = "state"
        const val INTENT_EXTRA_DL_NO = "dl_no"
        private const val REQUEST_CAPTURE_IMAGE = 1011
        private const val REQUEST_PICK_IMAGE = 1012

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    var verificationScreenStatus = VerificationScreenStatus.DEFAULT
    var ocrOrVerificationRquested = false
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    @Inject
    lateinit var navigation: INavigation
    @Inject

    lateinit var eventTracker: IEventTracker
    @Inject
    lateinit var buildConfig: IBuildConfig
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: DrivingLicenseViewModel by viewModels()
    private lateinit var viewBinding: DrivingLicenseFragmentBinding
    private var userId: String? = null
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }
    private var userIdToUse: String? = null
    private var dlFrontImagePath: Uri? = null
    private var dlBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DrivingLicenseFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    private val WAITING_TIME: Long = 1000 * 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreAndCommonUtilInterface.saveDataBoolean(
            com.gigforce.common_ui.StringConstants.DRIVING_LICENCE_SP.value,
            false
        )
        getDataFromIntents(savedInstanceState)
        initviews()
        setViews()
        listeners()
        observer()
        //checkForCroppedImage()

    }

    private fun initviews() {
        viewBinding.toplayoutblock.setIdonthaveDocContent(
            resources.getString(R.string.no_doc_title_dl_veri),
            resources.getString(R.string.no_doc_subtitle_dl_veri)
        )
        userIdToUse = if (userId != null) {
            userId
        }else{
            user?.uid
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
        outState.putString(AppConstants.INTENT_EXTRA_UID, userId)
    }

    var allNavigationList = ArrayList<String>()
    var intentBundle : Bundle? = null
    private fun getDataFromIntents(savedInstanceState: Bundle?) {
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

    private fun checkForCroppedImage() {
        var navFragmentsData = activity as NavFragmentsData
        if (navFragmentsData?.getData() != null) {
            if (navFragmentsData?.getData()?.getString(StringConstants.IMAGE_CROP_URI.value)?.isNotEmpty() == true) {
                val imageUriResultCrop = Uri.parse(navFragmentsData?.getData()?.getString(StringConstants.IMAGE_CROPPED_URI.value, ""))
                if (imageUriResultCrop != null) {
                    Log.d("Result crop", "cropped uri : $imageUriResultCrop")
                    if (DrivingLicenseSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                        dlFrontImagePath = imageUriResultCrop
                        showFrontDrivingLicense(dlFrontImagePath!!)
                    } else if (DrivingLicenseSides.BACK_SIDE == currentlyClickingImageOfSide) {
                        dlBackImagePath = imageUriResultCrop
                        showBackDrivingLicense(dlBackImagePath!!)
                    }
                }

                    }
            else {
                navFragmentsData?.setData(bundleOf())
            }
        }
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
                allNavigationList.get(0),intentBundle)

//            navigation.navigateTo(
//                allNavigationList.get(0),
//                bundleOf(
//                    VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle,
//                    if (FROM_CLIENT_ACTIVATON) StringConstants.FROM_CLIENT_ACTIVATON.value to true else StringConstants.FROM_CLIENT_ACTIVATON.value to false
//                )
//            )

        }
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            if (!manuallyRequestBackpress) { // || viewBinding.toplayoutblock.isDocDontOptChecked() || (!anyDataEntered &&  (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.FAILED))
                var navFragmentsData = activity as NavFragmentsData
                navFragmentsData.setData(
                    bundleOf(
                        StringConstants.BACK_PRESSED.value to true

                    )
                )
            }
        }
        return false
    }

    var anyDataEntered = false

    inner class ValidationTextWatcher : TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            context?.let { cxt ->
                if (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.FAILED || verificationScreenStatus == VerificationScreenStatus.OCR_COMPLETED) {
                    text?.let {

                        if (viewBinding.nameTilDl.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.dlnoTil.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.issueDate.text.toString()
                                .isNullOrBlank() && viewBinding.expiryDate.text.toString()
                                .isNullOrBlank() && viewBinding.dobDate.text.toString()
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

        viewBinding.nameTilDl.editText?.addTextChangedListener(ValidationTextWatcher())
        viewBinding.dlnoTil.editText?.addTextChangedListener(ValidationTextWatcher())
        viewBinding.issueDate.addTextChangedListener(ValidationTextWatcher())
        viewBinding.expiryDate.addTextChangedListener(ValidationTextWatcher())
        viewBinding.dobDate.addTextChangedListener(ValidationTextWatcher())


        viewBinding.stateSpinner.keyListener = null
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            checkForPermissionElseShowCameraGalleryBottomSheet()
            //if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) openCameraAndGalleryOptionForFrontSideImage() else openCameraAndGalleryOptionForBackSideImage()
        })

        viewBinding.dobDateRl.setOnClickListener {
            dateOfBirthPicker.show()
        }

        viewBinding.issueDateRl.setOnClickListener {
            issueDatePicker.show()
        }

        viewBinding.expiryDateRl.setOnClickListener {
            expiryDatePicker.show()
        }

        viewBinding.submitButton.setOnClickListener {
            hideSoftKeyboard()
            if (viewBinding.toplayoutblock.isDocDontOptChecked() || verificationScreenStatus == VerificationScreenStatus.VERIFIED || verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING || !anyDataEntered) {
                checkForNextDoc()
            } else {
                if (dlFrontImagePath == null || dlFrontImagePath.toString().isBlank()){
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.upload_dl_front_first_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                if (viewBinding.stateSpinner.text.equals("Select State")) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.select_dl_state_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (viewBinding.dlnoTil.editText?.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.select_dl_no_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (viewBinding.dobDate.text.toString().isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.select_dl_dob_veri))
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
        viewBinding.appBarDl.apply {
            setBackButtonListener{
                activity?.onBackPressed()
            }
        }
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

    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            verificationScreenStatus = VerificationScreenStatus.OCR_COMPLETED
            it?.let {
                if (it.status) {
                    if (!it.dateOfBirth.isNullOrBlank() || !it.dlNumber.isNullOrBlank() || !it.validTill.isNullOrBlank()) {
                        var map = mapOf(
                            "DL number" to it.dlNumber.toString(),
                            "DoB" to it.dateOfBirth.toString(),
                            "Expiry date" to it.validTill.toString()
                        )
                        eventTracker.pushEvent(
                            TrackingEventArgs(
                                eventName = VerificationEvents.DL_OCR_SUCCESS,
                                props = map
                            )
                        )
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            getString(R.string.upload_success_veri),
                            getString(R.string.info_of_dl_success_veri)
                        )
                        if (!it.dateOfBirth.isNullOrBlank()) {
                            if (it.dateOfBirth!!.contains("/") || it.dateOfBirth!!.contains("-")) {
                                viewBinding.dobDate.text = it.dateOfBirth
                                viewBinding.calendarLabel.visible()
                            }
                        }

                        if (!it.dlNumber.isNullOrBlank())
                            viewBinding.dlnoTil.editText?.setText(it.dlNumber)

                        if (!it.validTill.isNullOrBlank()) {
                            if (it.validTill!!.contains("-")) {
                                var dateInFormat = getDDMMYYYYFormat(it.validTill!!)
                                if (dateInFormat.isNotBlank())
                                    viewBinding.expiryDate.text = dateInFormat
                            } else if (it.validTill!!.contains("/"))
                                viewBinding.expiryDate.text = it.validTill
                        }

                    } else {
                        eventTracker.pushEvent(
                            TrackingEventArgs(
                                eventName = VerificationEvents.DL_OCR_SUCCESS,
                                props = mapOf("Data Captured" to false)
                            )
                        )
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            getString(R.string.unable_to_fetch_info_veri),
                            getString(R.string.enter_dl_details_veri)
                        )
                    }
                } else {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.DL_OCR_FAILED,
                            props = null
                        )
                    )
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UNABLE_TO_FETCH_DETAILS,
                        getString(R.string.unable_to_fetch_info_veri),
                        getString(R.string.enter_dl_details_veri)
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
                        props.put("DL verified", true)
                        eventTracker.setUserProperty(props)

                        verificationScreenStatus = VerificationScreenStatus.VERIFIED
                        verifiedStatusViews(it)
                        viewBinding.belowLayout.visible()
                        setAlreadyfilledData(it, false)
                        viewBinding.toplayoutblock.disableImageClick() //keep this line in end only
                    } else {
                        checkforStatusAndVerified(it)
                    }
                }
            }
        })
    }

    fun getDBImageUrl(imagePath: String): String? {
        if (imagePath.isNotBlank()) {
            try {
                var modifiedString = imagePath
                if (!imagePath.startsWith("/"))
                    modifiedString = "/$imagePath"
                return buildConfig.getStorageBaseUrl() + modifiedString
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    private fun getDDMMYYYYFormat(str: String): String {
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy")
            val date = sdf.parse(str)
            if (Date().after(date)) {
                return ""
            }
            return DateHelper.getDateInDDMMYYYY(date)
        } catch (e: Exception) {
            return ""
        }
    }

    private fun setViews() {
        //verification_doc_image    ic_dl_front ic_dl_back
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
                text = getString(R.string.upload_driving_license_front_side_new_veri),
                imageIcon = frontUri,
                imageUploaded = false
            ),
            KYCImageModel(
                text = getString(R.string.upload_driving_license_back_side_new_veri),
                imageIcon = backUri,
                imageUploaded = false
            )
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
        image?.let {
            eventTracker.pushEvent(TrackingEventArgs(VerificationEvents.DL_OCR_STARTED, null))
            viewModel.getKycOcrResult(
                "DL",
                if (currentlyClickingImageOfSide == DrivingLicenseSides.FRONT_SIDE) "front" else "back",
                it,
                userIdToUse.toString()
            )
        }
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            VerificationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                getString(R.string.upload_driving_veri),
                this
            )
        else
            requestStoragePermission()
    }

    private fun requestStoragePermission() {

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK){
            requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA
            ),
            REQUEST_STORAGE_PERMISSION
        )
        } else{
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

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK){
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else{
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

    private val issueDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewBinding.issueDate.text = DateHelper.getDateInDDMMYYYY(newCal.time)
                viewBinding.calendarLabel2.visible()
            },
            1990,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }
    private val expiryDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewBinding.expiryDate.text = DateHelper.getDateInDDMMYYYY(newCal.time)
                viewBinding.calendarLabel1.visible()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val maxDate = Calendar.getInstance()
        maxDate.set(Calendar.YEAR, maxDate.get(Calendar.YEAR) + 30)
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        datePickerDialog
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
                viewBinding.dobDate.text = DateHelper.getDateInDDMMYYYYHiphen(newCal.time)
                viewBinding.calendarLabel.visible()
            },
            1990,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
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
                        getString(R.string.upload_driving_veri),
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
                //startCrop(outputFileUri)
                startCropImage(outputFileUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
            Log.d("ImageUri", imageUriResultCrop.toString())
            if (DrivingLicenseSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                dlFrontImagePath = imageUriResultCrop
                showFrontDrivingLicense(dlFrontImagePath!!)
            } else if (DrivingLicenseSides.BACK_SIDE == currentlyClickingImageOfSide) {
                dlBackImagePath = imageUriResultCrop
                showBackDrivingLicense(dlBackImagePath!!)
            }

            val baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                val bitmap = data.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

            }
        } else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK){
            data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA)?.let {
                val imageUriResultCrop: Uri? =  Uri.parse(it)
                Log.d("ImageUri", imageUriResultCrop.toString())
                if (DrivingLicenseSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    dlFrontImagePath = imageUriResultCrop
                    showFrontDrivingLicense(dlFrontImagePath!!)
                } else if (DrivingLicenseSides.BACK_SIDE == currentlyClickingImageOfSide) {
                    dlBackImagePath = imageUriResultCrop
                    showBackDrivingLicense(dlBackImagePath!!)
                }
            }

        }
    }

    private fun callKycVerificationApi() {
        var list = listOf(
//                Data("state", viewBinding.stateSpinner.text.toString()),
            Data("name", viewBinding.nameTilDl.editText?.text.toString()),
            Data("no", viewBinding.dlnoTil.editText?.text.toString()),
//                Data("fathername", viewBinding.fatherNameTil.editText?.text.toString()),
            Data("issuedate", viewBinding.issueDate.text.toString()),
            Data("expirydate", viewBinding.expiryDate.text.toString()),
            Data("dob", viewBinding.dobDate.text.toString())
        )

        var map = mapOf(
            "DL number" to viewBinding.dlnoTil.editText?.text.toString(),
            "DoB" to viewBinding.dobDate.text.toString(),
            "Name" to viewBinding.nameTilDl.editText?.text.toString(),
            "Issue date" to viewBinding.issueDate.text.toString(),
            "Expiry date" to viewBinding.expiryDate.text.toString()
        )

        eventTracker.pushEvent(
            TrackingEventArgs(
                eventName = VerificationEvents.DL_DETAIL_SUBMITTED,
                props = map
            )
        )

        activeLoader(true)
        viewModel.getKycVerificationResult("DL", list, userIdToUse.toString())
    }

    private fun showFrontDrivingLicense(drivingFrontPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(0, drivingFrontPath)
        activeLoader(true)
        callKycOcrApi(drivingFrontPath)
    }

    private fun showBackDrivingLicense(drivingBackPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(1, drivingBackPath)
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_do_we_need_this_dl_veri)
        )
    }

    override fun onClickPictureThroughCameraClicked() {
        if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) {
            currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE
        } else {
            currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE
        }
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) {
            currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE
        } else {
            currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE
        }
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }
//    override fun onClickPictureThroughCameraClicked() {
//        if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) openCameraAndGalleryOptionForFrontSideImage() else openCameraAndGalleryOptionForBackSideImage()
//    }
//
//    override fun onPickImageThroughCameraClicked() {
//        if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) openCameraAndGalleryOptionForFrontSideImage() else openCameraAndGalleryOptionForBackSideImage()
//    }

    private fun startCropImage(imageUri: Uri): Unit {
       val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
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
        options.setHideBottomControls((true))
        options.setFreeStyleCropEnabled(true)
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

    private fun verifiedStatusViews(drivingLicenseDataModel: DrivingLicenseDataModel?) {
        viewBinding.toplayoutblock.viewChangeOnVerified()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.uploadStatusLayout(
            AppConstants.UPLOAD_SUCCESS,
            getString(R.string.verification_completed_veri),
            getString(R.string.dl_details_verified_veri)
        )
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = "Next"
        viewBinding.submitButton.isEnabled = true
        viewBinding.progressBar.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(getString(R.string.dl_verified_veri))

        var list = ArrayList<KYCImageModel>()
        drivingLicenseDataModel?.frontImage?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_driving_license_front_side_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        drivingLicenseDataModel?.backImage?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_driving_license_back_side_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        viewBinding.toplayoutblock.setImageViewPager(list)

    }

    private fun checkforStatusAndVerified(drivingLicenseDataModel: DrivingLicenseDataModel) {
        drivingLicenseDataModel.status?.let {
            when (it) {
                "started" -> {
                    verificationScreenStatus = VerificationScreenStatus.STARTED_VERIFYING
                    print("Driving Lincense started")
                    startedStatusViews(drivingLicenseDataModel)
                    Handler().postDelayed({
                        try {
                            if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
                                viewBinding.screenLoaderBar.gone()
                                verifiedStatusViews(null)
                                viewBinding.toplayoutblock.uploadStatusLayout(
                                    AppConstants.UNABLE_TO_FETCH_DETAILS,
                                    getString(R.string.verification_progress_veri),
                                    getString(R.string.document_verified_soon_veri)
                                )
                                viewBinding.toplayoutblock.setVerificationSuccessfulView("", "")
                                viewBinding.belowLayout.visible()
                                setAlreadyfilledData(drivingLicenseDataModel, false)
                                viewBinding.toplayoutblock.disableImageClick() //keep this line in end only
                            }
                        } catch (e: Exception) {

                        }

                    }, WAITING_TIME)
                    viewBinding.belowLayout.visible()
                    setAlreadyfilledData(drivingLicenseDataModel, false)
                    viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                }
                "failed" -> {
                    verificationScreenStatus = VerificationScreenStatus.FAILED
                    print("failed transaction")
                    resetInitializeViews()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.DETAILS_MISMATCH,
                        getString(R.string.verification_failed_veri),
                        getString(R.string.details_incorrect_veri)
                    )
                    var listData = setAlreadyfilledData(drivingLicenseDataModel, true)
                    if(listData.isEmpty()){
                        initializeImages()
                    }else{
                        //single if showing error
                    }
                    viewBinding.toplayoutblock.enableImageClick()//keep this line in end only
                }
                "" -> {
                    verificationScreenStatus = VerificationScreenStatus.DEFAULT
                    print("transaction reinitialized")
                    resetInitializeViews()
                    viewBinding.toplayoutblock.enableImageClick()//keep this line in end only
                }
                else -> "unmatched status"
            }
        }
    }

    private fun setAlreadyfilledData(
        drivingLicenseDataModel: DrivingLicenseDataModel,
        enableFields: Boolean
    ) : ArrayList<KYCImageModel> {

        viewBinding.nameTilDl.editText?.setText(drivingLicenseDataModel.name)

        viewBinding.dlnoTil.editText?.setText(drivingLicenseDataModel.dlNo)

        drivingLicenseDataModel.issuedate?.let {

            viewBinding.issueDate.text = it

            viewBinding.calendarLabel2.visible()

        }

        drivingLicenseDataModel.dob?.let {

            viewBinding.dobDate.text = DateHelper.getDateInDDMMYYYYHiphen(it)

            viewBinding.calendarLabel.visible()

        }

        drivingLicenseDataModel.expirydate?.let {

            viewBinding.expiryDate.text = DateHelper.getDateInDDMMYYYY(it)

            viewBinding.calendarLabel1.visible()

        }

        var list = ArrayList<KYCImageModel>()

        drivingLicenseDataModel.frontImage?.let {

            getDBImageUrl(it)?.let {

                list.add(

                    KYCImageModel(

                        text = getString(R.string.upload_driving_license_front_side_new_veri),

                        imagePath = it,

                        imageUploaded = true

                    )

                )
                dlFrontImagePath = Uri.parse(it)

            }

        }

        drivingLicenseDataModel.backImage?.let {

            getDBImageUrl(it)?.let {

                list.add(

                    KYCImageModel(

                        text = getString(R.string.upload_driving_license_back_side_new_veri),

                        imagePath = it,

                        imageUploaded = true

                    )

                )
                dlBackImagePath = Uri.parse(it)

            }

        }

        viewBinding.toplayoutblock.setImageViewPager(list)

        viewBinding.nameTilDl.editText?.isEnabled = enableFields
        viewBinding.dlnoTil.editText?.isEnabled = enableFields
        viewBinding.dobDateRl.isEnabled = enableFields
        viewBinding.dobDate.isEnabled = enableFields

        viewBinding.issueDateRl.isEnabled = enableFields
        viewBinding.issueDate.isEnabled = enableFields
        viewBinding.expiryDateRl.isEnabled = enableFields
        viewBinding.expiryDate.isEnabled = enableFields
        if (enableFields) {
            viewBinding.textView10.visible()
        } else {
            viewBinding.textView10.gone()
        }
        return list
    }

    private fun resetInitializeViews() {
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = getString(R.string.skip_veri)
        viewBinding.submitButton.isEnabled = true
        viewBinding.belowLayout.visible()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.driving_license_veri),
            getString(R.string.you_need_to_upload_veri)
        )
        initializeImages()
        viewBinding.toplayoutblock.resetAllViews()
    }

    private fun initializeImages() {
        // verification_doc_image ic_passbook_illustration
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
                text = getString(R.string.upload_driving_license_front_side_new_veri),
                imageIcon = frontUri,
                imageUploaded = false
            ),
            KYCImageModel(
                text = getString(R.string.upload_driving_license_back_side_new_veri),
                imageIcon = backUri,
                imageUploaded = false
            )
        )

        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun startedStatusViews(drivingLicenseDataModel: DrivingLicenseDataModel) {
        viewBinding.toplayoutblock.viewChangeOnStarted()
        viewBinding.screenLoaderBar.visible()
        viewBinding.submitButton.gone()
        viewBinding.progressBar.gone()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.dl_pending_verification_veri),
            getString(R.string.verifying_veri)
        )
        var list = ArrayList<KYCImageModel>()
        drivingLicenseDataModel.frontImage?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_driving_license_front_side_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        drivingLicenseDataModel.backImage?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_driving_license_back_side_new_veri),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

}