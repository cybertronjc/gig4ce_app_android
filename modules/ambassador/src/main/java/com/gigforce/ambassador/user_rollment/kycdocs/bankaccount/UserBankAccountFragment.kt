package com.gigforce.ambassador.user_rollment.kycdocs.bankaccount

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.ambassador.databinding.UserBankAccountFragmentBinding
import com.gigforce.ambassador.user_rollment.kycdocs.Data
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationClickOrSelectImageBottomSheet
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationConstants
import com.gigforce.ambassador.user_rollment.kycdocs.WhyWeNeedThisBottomSheet
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.VerificationValidations
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaeger.library.StatusBarUtil
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component_ambassador.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class VerificationScreenStatus {
    OCR_COMPLETED,
    VERIFIED,
    STARTED_VERIFYING,
    FAILED,
    COMPLETED,
    DEFAULT
}

@AndroidEntryPoint
class BankAccountFragment : Fragment(),
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener, IOnBackPressedOverride {

    companion object {
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
        private const val REQUEST_CAPTURE_IMAGE = 1012
        private const val REQUEST_PICK_IMAGE = 1013

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    var verificationScreenStatus = VerificationScreenStatus.DEFAULT
    var ocrOrVerificationRquested = false

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var buildConfig: IBuildConfig
    private lateinit var viewModelUser: UserBankAccountViewModel
    private var didUserCameFromAmbassadorScreen = false
    private var clickedImagePath: Uri? = null
    private lateinit var viewBinding: UserBankAccountFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = UserBankAccountFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelUser = ViewModelProviders.of(this).get(UserBankAccountViewModel::class.java)
        getDataFromIntent(savedInstanceState)
        initializeImages()
        observer()
        listeners()
    }

    private var userId: String? = ""
    private var userName: String? = ""
    var allNavigationList = ArrayList<String>()
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: ""
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: ""
        } ?: run {
            arguments?.let {
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
                userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: ""
                userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: ""
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
    }

    private fun observer() {
        viewModelUser.kycOcrResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            verificationScreenStatus = VerificationScreenStatus.OCR_COMPLETED
            it?.let {
                if (it.status) {
                    if (!it.beneficiaryName.isNullOrBlank() || !it.accountNumber.isNullOrBlank() || !it.ifscCode.isNullOrBlank() || !it.bankName.isNullOrBlank()) {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            "UPLOAD SUCCESSFUL",
                            "Information of Bank Captured Successfully."
                        )
                        if (!it.accountNumber.isNullOrBlank())
                            viewBinding.bankAccNumberItl.editText?.setText(it.accountNumber)
                        if (!it.ifscCode.isNullOrBlank())
                            viewBinding.ifscCode.editText?.setText(it.ifscCode)
                        if (!it.bankName.isNullOrBlank())
                            viewBinding.bankNameTil.editText?.setText(it.bankName)
                    } else {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "UNABLE TO FETCH DETAILS",
                            "Enter your Bank details manually or try again to continue the verification process."
                        )

                    }
                } else {
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UNABLE_TO_FETCH_DETAILS,
                        "UNABLE TO FETCH DETAILS",
                        "Enter your Bank details manually or try again to continue the verification process."
                    )
                    showToast("Ocr status " + it.message)
                }
            }
            ocrOrVerificationRquested = false
        })

        viewModelUser.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            ocrOrVerificationRquested = false
        })

        userId?.let {
            if (it.isNotBlank())
                viewModelUser.getBankVerificationUpdation(it)
        }


        viewModelUser.bankDetailedObject.observe(viewLifecycleOwner, Observer {
            Log.e("loaderissue","first")
            if (!ocrOrVerificationRquested) {
                viewBinding.screenLoaderBar.gone()
                Log.e("loaderissue","fifth")
                it?.let {

                    if (it.verified) {
                        verificationScreenStatus = VerificationScreenStatus.VERIFIED
                        verifiedStatusViews(it)
                    } else {
                        checkforStatusAndVerified(it)
                    }
                }
            }
        })


    }

    private fun startedStatusViews(bankDetailsDataModel: BankDetailsDataModel) {
        viewBinding.toplayoutblock.viewChangeOnStarted()
        viewBinding.screenLoaderBar.visible()
        Log.e("loaderissue","second")
        viewBinding.submitButton.gone()
        viewBinding.progressBar.gone()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            "Bank Account pending for verify",
            "Verifying"
        )
        var list = ArrayList<KYCImageModel>()
        bankDetailsDataModel.passbookImagePath?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_pan_card_new),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        viewBinding.toplayoutblock.setImageViewPager(list)
        viewBinding.confirmBeneficiaryLayout.gone()
    }

    private fun verifiedStatusViews(bankDetailsDataModel: BankDetailsDataModel?) {
        viewBinding.toplayoutblock.viewChangeOnVerified()
        viewBinding.belowLayout.gone()
        viewBinding.confirmBeneficiaryLayout.gone()
        viewBinding.toplayoutblock.uploadStatusLayout(
            AppConstants.UPLOAD_SUCCESS,
            "VERIFICATION COMPLETED",
            "The Bank Details have been verified successfully."
        )
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = "Next"
        viewBinding.submitButton.isEnabled = true
        viewBinding.progressBar.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView("Bank Account verified")

        var list = ArrayList<KYCImageModel>()
        bankDetailsDataModel?.passbookImagePath?.let {
            getDBImageUrl(it)?.let {
                list.add(
                    KYCImageModel(
                        text = getString(R.string.upload_pan_card_new),
                        imagePath = it,
                        imageUploaded = true
                    )
                )
            }
        }
        viewBinding.toplayoutblock.setImageViewPager(list)

    }

    private val SPLASH_TIME_OUT: Long = 1000 * 5
    private fun checkforStatusAndVerified(obj: BankDetailsDataModel) {
        obj.status?.let {
            when (it) {
                "started" -> {
                    verificationScreenStatus = VerificationScreenStatus.STARTED_VERIFYING
                    print("Bank Verification started")
                    startedStatusViews(obj)
                    Handler().postDelayed({
                        try {
                            if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
                                viewBinding.screenLoaderBar.gone()
                                Log.e("loaderissue","sixth")
                                verifiedStatusViews(null)
                                viewBinding.toplayoutblock.uploadStatusLayout(
                                    AppConstants.UNABLE_TO_FETCH_DETAILS,
                                    "VERIFICATION IN PROGRESS",
                                    "Click next to proceed. Verification will be done in parallel"
                                )
                                viewBinding.toplayoutblock.setVerificationSuccessfulView("", "")
                            }
                        } catch (e: Exception) {

                        }
                    }, SPLASH_TIME_OUT)
                }
                "failed" -> {
                    verificationScreenStatus = VerificationScreenStatus.FAILED
                    print("failed transaction")
                    resetInitializeViews()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.DETAILS_MISMATCH,
                        "VERIFICATION FAILED",
                        "Please recheck the information and try again"
                    )
                }
                "" -> {
                    verificationScreenStatus = VerificationScreenStatus.DEFAULT
                    print("transaction reinitialized")
                    resetInitializeViews()
                }
                "completed" -> {
                    verificationScreenStatus = VerificationScreenStatus.COMPLETED
                    print("beneficiary name will show")
                    showBankBeneficiaryName(obj)
                }
                else -> "unmatched status"
            }
        }
    }

    private fun resetInitializeViews() {
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = "Submit"
        viewBinding.submitButton.isEnabled = true
        viewBinding.belowLayout.visible()
        viewBinding.confirmBeneficiaryLayout.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            "Bank Account",
            "You need to upload"
        )
        initializeImages()
        viewBinding.toplayoutblock.resetAllViews()
    }

    private fun showBankBeneficiaryName(obj: BankDetailsDataModel) {
        obj.bankBeneficiaryName?.let { beneficiary ->
            if (beneficiary.isNotBlank()) {
                viewBinding.toplayoutblock.viewChangeOnStarted()
                viewBinding.confirmBeneficiaryLayout.visible()
                viewBinding.belowLayout.gone()
                viewBinding.beneficiaryName.text = beneficiary
                viewBinding.toplayoutblock.setVerificationSuccessfulView(
                    "Bank Account pending for verify",
                    "Verifying"
                )
                var list = ArrayList<KYCImageModel>()
                obj.passbookImagePath?.let {
                    getDBImageUrl(it)?.let {
                        list.add(
                            KYCImageModel(
                                text = getString(R.string.upload_pan_card_new),
                                imagePath = it,
                                imageUploaded = true
                            )
                        )
                    }
                }
                viewBinding.toplayoutblock.setImageViewPager(list)
            }
        }
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

    private fun activeLoader(activate: Boolean) {
        if (activate) {
            viewBinding.progressBar.visible()
            viewBinding.screenLoaderBar.visible()
            Log.e("loaderissue","third")
            viewBinding.submitButton.isEnabled = false
        } else {
            viewBinding.progressBar.gone()
            viewBinding.screenLoaderBar.gone()
            Log.e("loaderissue","forth")
            viewBinding.submitButton.isEnabled = true
        }
    }

    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            //showCameraAndGalleryOption()
            checkForPermissionElseShowCameraGalleryBottomSheet()
        })
        viewBinding.submitButton.setOnClickListener {
            hideSoftKeyboard()

            if (viewBinding.toplayoutblock.isDocDontOptChecked() || verificationScreenStatus == VerificationScreenStatus.VERIFIED || verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING || verificationScreenStatus == VerificationScreenStatus.COMPLETED) {
                checkForNextDoc()
            } else {
                val ifsc =
                    viewBinding.ifscCode.editText?.text.toString().toUpperCase(Locale.getDefault())
                if (!VerificationValidations.isIfSCValid(ifsc)) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.enter_valid_ifsc))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (viewBinding.bankNameTil.editText?.text.toString().isNullOrBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.enter_bank_name))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                if (viewBinding.bankNameTil.editText?.text.toString().length < 3) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.bank_name_too_short))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (viewBinding.bankAccNumberItl.editText?.text.toString().length < 4) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.enter_valid_acc_no))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
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

        viewBinding.appBarBank.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
        viewBinding.confirmButton.setOnClickListener {
            userId?.let {
                if (it.isNotBlank()) {
                    viewModelUser.setVerificationStatusInDB(it, true)
                    viewBinding.toplayoutblock.hideOnVerifiedDocuments()
                }
            }

        }
        viewBinding.notConfirmButton.setOnClickListener {
            userId?.let {
                if (it.isNotBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Do you want to re-enter Bank details?")
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModelUser.setVerificationStatusStringToBlank(it)
                        }
                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                else{
                    showToast("User ID not found!!")
                }
            }?: run {
                showToast("User ID not found!!")
            }

        }
    }

    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle =
                    allNavigationList.slice(IntRange(1, allNavigationList.size - 1))
                        .filter { it.length > 0 }
            }
            navigation.popBackStack()
            navigation.navigateTo(
                allNavigationList.get(0), bundleOf(
                    VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle,
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
                )
            )

        }
    }

    private fun initializeImages() {
        // verification_doc_image ic_passbook_illustration
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.verification_doc_image))
            .appendPath(resources.getResourceTypeName(R.drawable.verification_doc_image))
            .appendPath(resources.getResourceEntryName(R.drawable.verification_doc_image))
            .build()
        val list =
            listOf(
                KYCImageModel(
                    text = getString(R.string.upload_bank_account_new),
                    imageIcon = frontUri,
                    imageUploaded = false
                )
            )
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun callKycOcrApi(path: Uri) {
        ocrOrVerificationRquested = true
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
        image?.let { multipart ->
            userId?.let {
                if (it.isNotBlank())
                    viewModelUser.getKycOcrResult(it, "bank", "dummy", multipart)
            }
        }
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            VerificationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                "Upload Bank Passbook",
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
                        "Upload Bank Passbook",
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
            val outputFileUri =
                ImagePicker.getImageFromResult(requireContext(), resultCode, data)
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

    }


    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this),
            content = getString(R.string.why_do_we_need_this_bank)
        )
    }

    private fun callKycVerificationApi() {
        var list = listOf(
            Data("name", viewBinding.bankNameTil.editText?.text.toString()),
            Data("no", viewBinding.bankAccNumberItl.editText?.text.toString()),
            Data("ifsccode", viewBinding.ifscCode.editText?.text.toString())
        )
        activeLoader(true)
        ocrOrVerificationRquested = true
        userId?.let {
            if (it.isNotBlank())
                viewModelUser.getKycVerificationResult(it, "bank", list)
        }
    }

    private fun showPassbookInfoCard(bankInfoPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(0, bankInfoPath)
        activeLoader(true)
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
        options.setToolbarTitle(getString(R.string.crop_and_rotate))
        return options
    }

//    override fun onBackPressed(): Boolean {
//        if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
//            reContinueDialog()
//            return true
//        } else return false
//
//    }
    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert))
            .setMessage(getString(R.string.are_u_sure_u_want_to_go_back))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> goBackToUsersList() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }
    private fun goBackToUsersList() {
        findNavController().navigateUp()
    }
    private fun reContinueDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Do you want to wait?")
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                navigation.popBackStack()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
    }
}