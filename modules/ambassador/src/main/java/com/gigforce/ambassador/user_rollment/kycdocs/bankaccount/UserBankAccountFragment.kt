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
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
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
import com.gigforce.ambassador.user_rollment.kycdocs.*
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
        initViews()
        initializeImages()
        observer()
        listeners()
    }

    private fun initViews() {

        viewBinding.toplayoutblock.setIdonthaveDocContent(resources.getString(R.string.no_doc_title_bank),"")

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
                    if (!it.accountNumber.isNullOrBlank() || !it.ifscCode.isNullOrBlank() || !it.bankName.isNullOrBlank()) {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            getString(R.string.upload_succcess),
                            getString(R.string.bank_info_captured)
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
                            getString(R.string.unable_to_fetch_info),
                            getString(R.string.enter_bank_manually)
                        )

                    }
                } else {
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UNABLE_TO_FETCH_DETAILS,
                        getString(R.string.unable_to_fetch_info),
                        getString(R.string.enter_bank_manually)
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
                        viewBinding.belowLayout.visible()
                        setAlreadyfilledData(it, false)
                        viewBinding.toplayoutblock.toggleChangeTextView(true)
                        viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
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
        viewBinding.submitButton.gone()
        viewBinding.progressBar.gone()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.toggleChangeTextView(false)
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.bank_verification_pending),
            getString(R.string.verifying)
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
            getString(R.string.verification_completed),
            getString(R.string.bank_verification_success)
        )
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = "Next"
        viewBinding.submitButton.isEnabled = true
        viewBinding.progressBar.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(getString(R.string.bank_verified))

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

    private val WAITING_TIME: Long = 1000 * 3
    private fun checkforStatusAndVerified(obj: BankDetailsDataModel) {
        obj.status?.let {
            when (it) {
                "started" -> {
                    verificationScreenStatus = VerificationScreenStatus.STARTED_VERIFYING
                    startedStatusViews(obj)
                    Handler().postDelayed({
                        try {
                            if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
                                viewBinding.screenLoaderBar.gone()
                                verifiedStatusViews(null)
                                viewBinding.toplayoutblock.uploadStatusLayout(
                                    AppConstants.UNABLE_TO_FETCH_DETAILS,
                                    getString(R.string.verification_progress),
                                    getString(R.string.doc_verified_soon)
                                )
                                viewBinding.toplayoutblock.setVerificationSuccessfulView("", "")
//                                viewBinding.editBankDetail.visible()
                                viewBinding.belowLayout.visible()
                                setAlreadyfilledData(obj, false)
                                viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                            }
                        } catch (e: Exception) {

                        }
                    }, WAITING_TIME)
                    viewBinding.belowLayout.visible()
                    setAlreadyfilledData(obj, false)
                    viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                }
                "failed" -> {
                    verificationScreenStatus = VerificationScreenStatus.FAILED
                    resetInitializeViews()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.DETAILS_MISMATCH,
                        getString(R.string.verification_failed),
                        getString(R.string.details_incorrect)
                    )
                    var listData = setAlreadyfilledData(obj, true)
                    if (listData.isEmpty()) {
                        initializeImages()
                    }
                    viewBinding.toplayoutblock.toggleChangeTextView(false)
                    viewBinding.toplayoutblock.enableImageClick()//keep this line in end only
                }
                "" -> {
                    verificationScreenStatus = VerificationScreenStatus.DEFAULT
                    resetInitializeViews()
                    viewBinding.toplayoutblock.toggleChangeTextView(false)
                    viewBinding.toplayoutblock.enableImageClick()//keep this line in end only
                }
                "completed" -> {
                    verificationScreenStatus = VerificationScreenStatus.COMPLETED
                    showBankBeneficiaryName(obj)
                    viewBinding.toplayoutblock.toggleChangeTextView(false)
                    viewBinding.toplayoutblock.disableImageClick()//keep this line in end only
                }
                else -> "unmatched status"
            }
        }
    }

    private fun setAlreadyfilledData(
        obj1: BankDetailsDataModel?,
        enableFields: Boolean
    ): ArrayList<KYCImageModel> {
        var list = ArrayList<KYCImageModel>()
        obj1?.let { obj ->
            viewBinding.bankNameTil.editText?.setText(obj.bankName)

            viewBinding.bankAccNumberItl.editText?.setText(obj.accountNo)

            viewBinding.ifscCode.editText?.setText(obj.ifscCode)



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



        viewBinding.bankNameTil.editText?.isEnabled = enableFields
        viewBinding.bankAccNumberItl.editText?.isEnabled = enableFields
        viewBinding.ifscCode.editText?.isEnabled = enableFields
        if (enableFields) {
            viewBinding.textView10.visible()
        } else {
            viewBinding.textView10.gone()
        }
        return list
    }

    private fun resetInitializeViews() {
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = "Skip"
        viewBinding.submitButton.isEnabled = true
        viewBinding.belowLayout.visible()
        viewBinding.progressBar.gone()
        viewBinding.confirmBeneficiaryLayout.gone()
        viewBinding.toplayoutblock.toggleChangeTextView(false)
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.bank_account),
            getString(R.string.need_to_upload)
        )
        initializeImages()
        viewBinding.toplayoutblock.resetAllViews()

        viewBinding.bankAccNumberItl.editText?.setText("")
        viewBinding.bankNameTil.editText?.setText("")
        viewBinding.ifscCode.editText?.setText("")
    }

    private fun showBankBeneficiaryName(obj: BankDetailsDataModel) {
        obj.bankBeneficiaryName?.let { beneficiary ->
            if (beneficiary.isNotBlank()) {
                viewBinding.toplayoutblock.viewChangeOnStarted()
                viewBinding.confirmBeneficiaryLayout.visible()
                viewBinding.belowLayout.gone()
                viewBinding.submitButton.gone()
                viewBinding.progressBar.gone()
                viewBinding.beneficiaryName.text = beneficiary
                viewBinding.toplayoutblock.setVerificationSuccessfulView(
                    getString(R.string.verification_pending),
                    getString(R.string.verifying)
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
            viewBinding.submitButton.isEnabled = false
        } else {
            viewBinding.progressBar.gone()
            viewBinding.screenLoaderBar.gone()
            viewBinding.submitButton.isEnabled = true
        }
    }

    var anyDataEntered = false

    inner class ValidationTextWatcher : TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            context?.let { cxt ->
                if (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.FAILED || verificationScreenStatus == VerificationScreenStatus.OCR_COMPLETED) {
                    text?.let {
                        if (viewBinding.bankNameTil.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.bankAccNumberItl.editText?.text.toString()
                                .isNullOrBlank() && viewBinding.ifscCode.editText?.text.toString()
                                .isNullOrBlank()
                        ) {
                            viewBinding.submitButton.text = "Skip"
                            anyDataEntered = false
                        } else {
                            viewBinding.submitButton.text = "Submit"
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
                viewBinding.submitButton.text = "Skip"
                viewBinding.belowLayout.gone()
            } else {
                viewBinding.submitButton.text = oldStateHolder.submitButtonCta
                viewBinding.belowLayout.visible()
            }

        })
        viewBinding.bankNameTil.editText?.addTextChangedListener(ValidationTextWatcher())
        viewBinding.bankAccNumberItl.editText?.addTextChangedListener(ValidationTextWatcher())
        viewBinding.ifscCode.editText?.addTextChangedListener(ValidationTextWatcher())

        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            //showCameraAndGalleryOption()
            checkForPermissionElseShowCameraGalleryBottomSheet()
        })

        viewBinding.toplayoutblock.setChangeTextListener(View.OnClickListener {
            resetInitializeViews()
            viewBinding.toplayoutblock.toggleChangeTextView(false)
            setAlreadyfilledData(null, true)
            verificationScreenStatus = VerificationScreenStatus.DEFAULT
            viewBinding.submitButton.text = "Skip"
        })

        viewBinding.submitButton.setOnClickListener {
            hideSoftKeyboard()

            if (viewBinding.toplayoutblock.isDocDontOptChecked() || verificationScreenStatus == VerificationScreenStatus.VERIFIED || verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING || verificationScreenStatus == VerificationScreenStatus.COMPLETED || !anyDataEntered) {
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
//                navigation.popBackStack()
                activity?.onBackPressed()
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
                        .setTitle(getString(R.string.want_to_reenter_details))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModelUser.setVerificationStatusStringToBlank(it)
                        }
                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                else{
                    showToast(getString(R.string.user_not_found))
                }
            }?: run {
                showToast(getString(R.string.user_not_found))
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
//            navigation.popBackStack()
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
                getString(R.string.upload_passbook),
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
                        getString(R.string.upload_passbook),
                        this
                    )
                else {
                    showToast(getString(R.string.grant_storage_permission))
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
            .setTitle(getString(R.string.want_to_wait))
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