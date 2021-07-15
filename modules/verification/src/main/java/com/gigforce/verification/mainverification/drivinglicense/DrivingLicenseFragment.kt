package com.gigforce.verification.mainverification.drivinglicense

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.StringConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.R
import com.gigforce.verification.databinding.DrivingLicenseFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.verification.gigerVerfication.drivingLicense.DrivingLicenseSides
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
class DrivingLicenseFragment : Fragment(),
        VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener, IOnBackPressedOverride {
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

    @Inject
    lateinit var navigation: INavigation

    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: DrivingLicenseViewModel by viewModels()
    private lateinit var viewBinding: DrivingLicenseFragmentBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        setViews()
        observer()
        listeners()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)


    }

    var allNavigationList = ArrayList<String>()
    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
        } ?: run {
            arguments?.let {
                FROM_CLIENT_ACTIVATON =
                        it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }

            }
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

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            if (isDLVerified) {
                var navFragmentsData = activity as NavFragmentsData
                navFragmentsData.setData(
                        bundleOf(
                                StringConstants.BACK_PRESSED.value to true

                        )
                )
            }
            return false
        }
        return false
    }

    val CONFIRM_TAG: String = "confirm"

    private fun listeners() {
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
            if (toplayoutblock.isDocDontOptChecked()) {
                checkForNextDoc()
            } else {
                if (viewBinding.submitButton.tag?.toString().equals(CONFIRM_TAG)) {
                    checkForNextDoc()
                } else {
                    if (viewBinding.stateSpinner.text.equals( "Select State")) {
                        MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.select_dl_state))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()
                        return@setOnClickListener
                    }

                    if (viewBinding.dlnoTil.editText?.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.select_dl_no))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()
                        return@setOnClickListener
                    }

                    if (viewBinding.dobDate.text.toString().isBlank()) {
                        MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.select_dl_dob))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()
                        return@setOnClickListener
                    }

                    callKycVerificationApi()
                }
            }
        }

        viewBinding.toplayoutblock.querytext.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.imageView7.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.appBarDl.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
        val arrayAdapter = context?.let { it1 ->
            ArrayAdapter.createFromResource(
                    it1,
                    R.array.indian_states,
                    android.R.layout.simple_spinner_dropdown_item
            )
        }
        viewBinding.stateSpinner.setAdapter(arrayAdapter)
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

    var isDLVerified = false
    var anyImageUploaded = false
    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            it?.let {
                if (it.status) {
                    anyImageUploaded = true
                    if (!it.dateOfBirth.isNullOrBlank() || !it.dlNumber.isNullOrBlank() || !it.validTill.isNullOrBlank()) {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                                AppConstants.UPLOAD_SUCCESS,
                                "UPLOAD SUCCESSFUL",
                                "Information of Driving License Captured Successfully."
                        )
                        if (!it.dateOfBirth.isNullOrBlank())
                            viewBinding.dobDate.setText(it.dateOfBirth)
                        if (!it.dlNumber.isNullOrBlank())
                            viewBinding.dlnoTil.editText?.setText(it.dlNumber)

                        if (!it.validTill.isNullOrBlank()) {
                            if (it.validTill.contains("-")) {
                                var dateInFormat = getDDMMYYYYFormat(it.validTill)
                                if (dateInFormat.isNotBlank())
                                    viewBinding.expiryDate.text = dateInFormat
                            } else
                                viewBinding.expiryDate.text = it.validTill
                        }

                    } else {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                                AppConstants.UNABLE_TO_FETCH_DETAILS,
                                "UNABLE TO FETCH DETAILS",
                                "Enter your Driving License details manually or try again to continue the verification process."
                        )
                    }
                } else {
                    viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "UNABLE TO FETCH DETAILS",
                            "Enter your Driving License details manually or try again to continue the verification process."
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
                            AppConstants.UPLOAD_SUCCESS,
                            "VERIFICATION COMPLETED",
                            "The Driving License Details have been verified successfully."
                    )
                    viewBinding.submitButton.tag = CONFIRM_TAG
                    viewBinding.toplayoutblock.setVerificationSuccessfulView("Your Driving License verified")
                    viewBinding.submitButton.text = getString(R.string.submit)
                    viewBinding.toplayoutblock.disableImageClick()
                    viewBinding.toplayoutblock.hideOnVerifiedDocuments()
                    isDLVerified = true
                } else
                    showToast("Verification " + it.message)
            }
        })
        viewModel.getVerifiedStatus()
        viewModel.verifiedStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            "VERIFICATION COMPLETED",
                            "The Driving License Details have been verified successfully."
                    )
                    viewBinding.submitButton.tag = CONFIRM_TAG
                    viewBinding.toplayoutblock.setVerificationSuccessfulView("Your Driving License verified")
                    viewBinding.toplayoutblock.disableImageClick()
                    viewBinding.toplayoutblock.hideOnVerifiedDocuments()
                }
            }
        })
        viewModel.observableStates.observe(viewLifecycleOwner, Observer {
            it?.let {
                val stateList = arrayListOf<String>()
                it.forEach {
                    stateList.add(it.name)
                }
                Log.d("states", it.toString())
//                val arrayAdapter = context?.let { it1 -> ArrayAdapter(it1,android.R.layout.simple_spinner_item, stateList) }
//                stateSpinner.adapter = arrayAdapter
            }
        })

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
        val frontUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.ic_dl_front))
                .appendPath(resources.getResourceTypeName(R.drawable.ic_dl_front))
                .appendPath(resources.getResourceEntryName(R.drawable.ic_dl_front))
                .build()
        val backUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.ic_dl_back))
                .appendPath(resources.getResourceTypeName(R.drawable.ic_dl_back))
                .appendPath(resources.getResourceEntryName(R.drawable.ic_dl_back))
                .build()
        val list = listOf(
                KYCImageModel(
                        getString(R.string.upload_driving_license_front_side_new),
                        frontUri,
                        false
                ),
                KYCImageModel(getString(R.string.upload_driving_license_back_side_new), backUri, false)
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
            viewModel.getKycOcrResult(
                    "DL",
                    if (currentlyClickingImageOfSide == DrivingLicenseSides.FRONT_SIDE) "front" else "back",
                    it
            )
        }
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            VerificationClickOrSelectImageBottomSheet.launch(
                    parentFragmentManager,
                    "Upload Driving License",
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
                    viewBinding.dobDate.setText(DateHelper.getDateInDDMMYYYYHiphen(newCal.time))
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
                            "Upload Driving License",
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
        }

//                if (confirmDLDataCB_client_act.isChecked
//                    && dlFrontImagePath != null
//                    && dlBackImagePath != null
//                ) {
//                    enableSubmitButton()
//                }
//
//                if (dlFrontImagePath != null && dlBackImagePath != null && dlSubmitSliderBtn_client_act.isGone) {
//                    dlSubmitSliderBtn_client_act.visible()
//                    confirmDLDataCB_client_act.visible()
//                }


    }


//    private fun showDLImageAndInfoLayout() {
//        dlBackImageHolder.visibility = View.VISIBLE
//        dlFrontImageHolder.visibility = View.VISIBLE
//        showImageInfoLayout()
//    }
//
//    private fun hideDLImageAndInfoLayout() {
//        dlBackImageHolder.visibility = View.GONE
//        dlFrontImageHolder.visibility = View.GONE
//        dlInfoLayout.visibility = View.GONE
//    }
//
//    private fun enableSubmitButton() {
//        dlSubmitSliderBtn_client_act.isEnabled = true
//
//        dlSubmitSliderBtn_client_act.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        dlSubmitSliderBtn_client_act.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }
//
//    private fun disableSubmitButton() {
//        dlSubmitSliderBtn_client_act.isEnabled = false
//
//        dlSubmitSliderBtn_client_act.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        dlSubmitSliderBtn_client_act.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }
//
//
//    private fun showImageInfoLayout() {
//        dlInfoLayout.visibility = View.VISIBLE
//    }

    private fun callKycVerificationApi() {
        var list = listOf(
                Data("state", viewBinding.stateSpinner.text.toString()),
                Data("name", viewBinding.nameTilDl.editText?.text.toString()),
                Data("no", viewBinding.dlnoTil.editText?.text.toString()),
                Data("fathername", viewBinding.fatherNameTil.editText?.text.toString()),
                Data("issuedate", viewBinding.issueDate.text.toString()),
                Data("expirydate", viewBinding.expiryDate.text.toString()),
                Data("dob", viewBinding.dobDate.text.toString())
        )
        activeLoader(true)
        viewModel.getKycVerificationResult("DL", list)
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
                title = getString(R.string.why_do_we_need_this),
                content = getString(R.string.why_do_we_need_this_dl)
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