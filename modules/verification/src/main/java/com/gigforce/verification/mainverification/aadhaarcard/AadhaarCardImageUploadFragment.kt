package com.gigforce.verification.mainverification.aadhaarcard

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
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardImageUploadFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.verification.gigerVerfication.aadharCard.AadharCardSides
import com.gigforce.verification.mainverification.Data
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        setViews()
        observer()
        listeners()

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
            if(viewBinding.submitButton.getTag()?.toString().equals(CONFIRM_TAG)){
                activity?.onBackPressed()
            }else {
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

        viewBinding.toplayoutblock.querytext.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.imageView7.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.appBarAadhar.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
    }

    val CONFIRM_TAG: String = "confirm"
    private fun observer() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it.status) {
                    if (!it.aadhaarNumber.isNullOrBlank() || !it.gender.isNullOrBlank() || !it.dateOfBirth.isNullOrBlank()) {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            "UPLOAD SUCCESSFUL",
                            "Information of Aadhar Card Captured Successfully."
                        )
                        viewBinding.aadharcardTil.editText?.setText(it.aadhaarNumber)
                        viewBinding.nameTilAadhar.editText?.setText(it.name)
                        viewBinding.dateOfBirthAadhar.text = it.dateOfBirth
                    } else {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "UNABLE TO FETCH DETAILS",
                            "Enter your Aadhar details manually or try again to continue the verification process."
                        )
                    }
                } else {
                    showToast("Ocr status " + it.message)
                }
            }
        })

        viewModel.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it.status) {
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UPLOAD_SUCCESS,
                        "VERIFICATION COMPLETED",
                        "The Pan card Details have been verified successfully."
                    )
                    viewBinding.submitButton.tag = CONFIRM_TAG
                    viewBinding.toplayoutblock.setVerificationSuccessfulView()
                    viewBinding.submitButton.text = getString(R.string.submit)
                } else
                    showToast("Verification status " + it.message)
            }
        })
        viewModel.getVerifiedStatus()
        viewModel.verifiedStatus.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it){
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UPLOAD_SUCCESS,
                        "VERIFICATION COMPLETED",
                        "The Aadhaar Card Details have been verified successfully."
                    )
                    viewBinding.submitButtonAadhar.tag = CONFIRM_TAG
                    viewBinding.toplayoutblock.setVerificationSuccessfulView()
                }
            }
        })
    }

    val CONFIRM_TAG: String = "confirm"

    private fun setViews() {
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_front))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_front))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_front))
            .build()
        val backUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_back))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_back))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_back))
            .build()
        val list = listOf(
            KYCImageModel(
                getString(R.string.upload_aadhar_card_front_side_new),
                frontUri,
                false
            ), KYCImageModel(getString(R.string.upload_aadhar_card_back_side_new), backUri, false)
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
        image?.let { viewModel.getKycOcrResult("aadhar", "kjk", it) }
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
            Data("yearofbirth", viewBinding.dateOfBirthAadhar.text.toString())
        )
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


}