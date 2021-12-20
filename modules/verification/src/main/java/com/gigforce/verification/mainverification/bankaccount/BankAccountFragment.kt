package com.gigforce.verification.mainverification.bankaccount

import android.Manifest
import android.app.Activity
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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.remote.verification.Data
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.*
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.utils.VerificationValidations
import com.gigforce.verification.R
import com.gigforce.verification.databinding.BankAccountFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
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
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener,
    IOnBackPressedOverride {

    companion object {
        fun newInstance() = BankAccountFragment()
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
        private const val REQUEST_CAPTURE_IMAGE = 1012
        private const val REQUEST_PICK_IMAGE = 1013

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
        private const val IFSC_ZERO_TEXT_INDEX_POSITION = 4
    }

    var verificationScreenStatus = VerificationScreenStatus.DEFAULT
    var ocrOrVerificationRquested = false
    private var userId: String? = null
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }
    private var userIdToUse: String? = null
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var buildConfig: IBuildConfig
    private var FROM_CLIENT_ACTIVATON: Boolean = false
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
        sharedPreAndCommonUtilInterface.saveDataBoolean(
            com.gigforce.common_ui.StringConstants.BANK_DETAIL_SP.value,
            false
        )
        getDataFromIntent(savedInstanceState)
        initViews()

        initializeImages()
        observer {

        }
        listeners()
    }

    private fun initViews() {
        viewBinding.toplayoutblock.setIdonthaveDocContent(
            resources.getString(R.string.no_doc_title_bank_veri),
            resources.getString(R.string.no_doc_subtitle_bank_veri)
        )
        userIdToUse = if (userId != null) {
        viewBinding.confirmBn.text = resources.getString(R.string.bn_not_matched_veri)
        viewBinding.confirmBnDetail.text = resources.getString(R.string.plz_ask_to_giger_to_confirm_bn_veri)
        viewBinding.banificiaryDetail.gone()
        viewBinding.cancelButton.gone()
        viewBinding.confirmBnBs.gone()
            viewBinding.okayButton.visible()
            userId
        }else{
            viewBinding.cancelButton.visible()
            viewBinding.confirmBnBs.visible()
            viewBinding.okayButton.gone()
            user?.uid
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
        outState.putString(AppConstants.INTENT_EXTRA_UID, userId)
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

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            if (!manuallyRequestBackpress) { //|| viewBinding.toplayoutblock.isDocDontOptChecked() || (!anyDataEntered &&  (verificationScreenStatus == VerificationScreenStatus.DEFAULT || verificationScreenStatus == VerificationScreenStatus.FAILED) )
                var navFragmentsData = activity as NavFragmentsData
                navFragmentsData.setData(
                    bundleOf(
                        StringConstants.BACK_PRESSED.value to true

                    )
                )
            }
        }
        if(verificationScreenStatus == VerificationScreenStatus.COMPLETED){
            return true
        }
        return false
    }

    private fun observer(function: () -> Unit) {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            activeLoader(false)
            verificationScreenStatus = VerificationScreenStatus.OCR_COMPLETED
            it?.let {
                if (it.status) {
                    if (!it.accountNumber.isNullOrBlank() || !it.ifscCode.isNullOrBlank() || !it.bankName.isNullOrBlank()) {
                        val map = mapOf(
                            "Account number" to it.accountNumber.toString(),
                            "IFSC code" to it.ifscCode.toString(),
                            "Bank name" to it.bankName.toString()
                        )
                        eventTracker.pushEvent(
                            TrackingEventArgs(
                                eventName = VerificationEvents.BANK_OCR_SUCCESS,
                                props = map
                            )
                        )
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            getString(R.string.upload_success_veri),
                            getString(R.string.bank_info_success_veri)
                        )
                        if (!it.accountNumber.isNullOrBlank())
                            viewBinding.bankAccNumberItl.editText?.setText(it.accountNumber)
                        if (!it.ifscCode.isNullOrBlank())
                            viewBinding.ifscCode.editText?.setText(it.ifscCode)
                        if (!it.bankName.isNullOrBlank())
                            viewBinding.bankNameTil.editText?.setText(it.bankName)
                    } else {
                        eventTracker.pushEvent(
                            TrackingEventArgs(
                                eventName = VerificationEvents.BANK_OCR_SUCCESS,
                                props = mapOf("Data Captured" to false)
                            )
                        )
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            getString(R.string.unable_to_fetch_info_veri),
                            getString(R.string.enter_bank_details_manually_veri)
                        )

                    }
                } else {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.BANK_OCR_FAILED,
                            props = null
                        )
                    )
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UNABLE_TO_FETCH_DETAILS,
                        getString(R.string.unable_to_fetch_info_veri),
                        getString(R.string.enter_bank_details_manually_veri)
                    )
                    showToast(getString(R.string.ocr_status_veri) + it.message)
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

//        viewModel.getBankVerificationUpdation()
        viewModel.getBankDetailsStatus(userIdToUse.toString())
        viewModel.bankDetailedObject.observe(viewLifecycleOwner, Observer {
            if (!ocrOrVerificationRquested) {
                viewBinding.screenLoaderBar.gone()
                it?.let {
//                    if (it.verified && it.verified_source.toLowerCase() != "user_consent" && it.counter == null) {
//                        showUserAckPopup()
//                        verificationScreenStatus = VerificationScreenStatus.VERIFIED
//                        verifiedStatusViews(it)
//                        viewBinding.belowLayout.visible()
//                        setAlreadyfilledData(it, false)
//                        viewBinding.toplayoutblock.toggleChangeTextView(true)
//                        //viewBinding.toplayoutblock.disableImageClick()//keep this line in end only //need to remove uploading option 2856 ticket
//                    } else {
                        checkforStatusAndVerified(it)
//                    }
                }
            }
        })

        viewModel.userConsentModel.observe(viewLifecycleOwner,Observer{
            when(it){
                is Lce.Loading -> {
                    viewBinding.screenLoaderBar.visible()
                }
                is Lce.Content -> {
                    viewBinding.screenLoaderBar.gone()
                }
                is Lce.Error -> {
                    showToast(it.error)
                    viewBinding.screenLoaderBar.gone()
                }
            }
        })

    }

    private fun showUserAckPopup(benificiaryName : String) {
        //show bottomsheet to aknowledge user for beneficiary name
        // on popup user will press the button and api will call which will update the counter value
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle(benificiaryName)
//            .setCancelable(false
//            .setPositiveButton(getString(R.string.okay_lower_case_veri)) { _, _ ->
//                eventTracker.pushEvent(
//                    TrackingEventArgs(
//                        eventName = VerificationEvents.BANK_MISMATCH,
//                        props = null
//                    )
//                )
//                    viewModel.setUserAknowledge(userIdToUse.toString())
//            }
//             .show()
        navigation.navigateTo("verification/acknowledgeBankBS")
    }

    private fun startedStatusViews(bankDetailsDataModel: BankDetailsDataModel) {
        viewBinding.toplayoutblock.viewChangeOnStarted()
        viewBinding.screenLoaderBar.visible()
        viewBinding.submitButton.gone()
        viewBinding.progressBar.gone()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.toggleChangeTextView(false)
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.bank_verification_pending_veri),
            getString(R.string.verifying_veri)
        )
        var list = ArrayList<KYCImageModel>()
        bankDetailsDataModel.passbookImagePath?.let {
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
//        viewBinding.toplayoutblock.setImageViewPager(list) need to remove uploading option 2856 ticket
        viewBinding.confirmBeneficiaryLayout.gone()
    }

    private fun verifiedStatusViews(bankDetailsDataModel: BankDetailsDataModel?) {
        viewBinding.toplayoutblock.viewChangeOnVerified()
        viewBinding.belowLayout.gone()
        viewBinding.confirmBeneficiaryLayout.gone()
        viewBinding.toplayoutblock.uploadStatusLayout(
            AppConstants.UPLOAD_SUCCESS,
            getString(R.string.verification_completed_veri),
            getString(R.string.bank_details_verified_veri)
        )
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = getString(R.string.next_camel_veri)
        viewBinding.submitButton.isEnabled = true
        viewBinding.progressBar.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView(getString(R.string.bank_verified_veri))

        var list = ArrayList<KYCImageModel>()
        bankDetailsDataModel?.passbookImagePath?.let {
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
//        viewBinding.toplayoutblock.setImageViewPager(list) need to remove uploading option 2856 ticket

    }

    private val WAITING_TIME: Long = 1000 * 3
    private fun checkforStatusAndVerified(obj: BankDetailsDataModel) {
        obj.status?.let {
            when (it) {
                "verified"->{
                    if(obj.verified_source.toLowerCase() != "user_consent" && obj.counter == null) {
                        showUserAckPopup(obj.bankBeneficiaryName ?: "")
                    }
                    verificationScreenStatus = VerificationScreenStatus.VERIFIED
                    verifiedStatusViews(obj)
                    viewBinding.belowLayout.visible()
                    setAlreadyfilledData(obj, false)
                    viewBinding.toplayoutblock.toggleChangeTextView(true)
                    viewBinding.bnConfirmationCl.gone()
                    viewBinding.scrollView.visible()
                    viewBinding.toplayoutblock.visible()

                }
                "started","processing","validated" -> {
                    verificationScreenStatus = VerificationScreenStatus.STARTED_VERIFYING
                    startedStatusViews(obj)
                    Handler().postDelayed({
                        try {
                            if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
                                viewBinding.screenLoaderBar.gone()
                                verifiedStatusViews(null)
                                viewBinding.toplayoutblock.uploadStatusLayout(
                                    AppConstants.UNABLE_TO_FETCH_DETAILS,
                                    getString(R.string.verification_progress_veri),
                                    getString(R.string.verified_soon_veri)
                                )
                                viewBinding.toplayoutblock.setVerificationSuccessfulView("", "")
//                                viewBinding.editBankDetail.visible()
                                viewBinding.belowLayout.visible()
                                setAlreadyfilledData(obj, false)
                                viewBinding.bnConfirmationCl.gone()
                                viewBinding.scrollView.visible()

                                //viewBinding.toplayoutblock.disableImageClick()//keep this line in end only  //need to remove uploading option 2856 ticket
                            }
                        } catch (e: Exception) {

                        }
                    }, WAITING_TIME)
                    viewBinding.belowLayout.visible()
                    setAlreadyfilledData(obj, false)
                    viewBinding.bnConfirmationCl.gone()
                    viewBinding.scrollView.visible()

                    //viewBinding.toplayoutblock.disableImageClick()//keep this line in end only //need to remove uploading option 2856 ticket
                }
                "validation_failed" -> {
                    verificationScreenStatus = VerificationScreenStatus.FAILED
                    resetInitializeViews()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.DETAILS_MISMATCH,
                        getString(R.string.verification_failed_veri),
                        getString(R.string.details_incorrect_veri)
                    )
                    var listData = setAlreadyfilledData(obj, true)
                    if (listData.isEmpty()) {
                        initializeImages()
                    }
                    viewBinding.toplayoutblock.toggleChangeTextView(false)
                    viewBinding.bnConfirmationCl.gone()
                    viewBinding.scrollView.visible()

                    //viewBinding.toplayoutblock.enableImageClick()//keep this line in end only //need to remove uploading option 2856 ticket
                }
                "","rejected" -> {
                    verificationScreenStatus = VerificationScreenStatus.DEFAULT
                    resetInitializeViews()
                    setAlreadyfilledData(null, true)
                    viewBinding.toplayoutblock.toggleChangeTextView(false)
                    viewBinding.bnConfirmationCl.gone()
                    viewBinding.scrollView.visible()

                    //viewBinding.toplayoutblock.enableImageClick()//keep this line in end only //need to remove uploading option 2856 ticket
                }
                "verification_pending" -> {
                    verificationScreenStatus = VerificationScreenStatus.COMPLETED
                    showBankBeneficiaryName(obj)
//                    viewBinding.toplayoutblock.toggleChangeTextView(false)
                    //viewBinding.toplayoutblock.disableImageClick()//keep this line in end only  //need to remove uploading option 2856 ticket
//                    verificationScreenStatus = VerificationScreenStatus.COMPLETED

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

                            text = getString(R.string.upload_pan_card_new_veri),

                            imagePath = it,

                            imageUploaded = true

                        )
                    )
                }
            }
//            viewBinding.toplayoutblock.setImageViewPager(list) need to remove uploading option 2856 ticket

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
        viewBinding.submitButton.text = getString(R.string.skip_veri)
        viewBinding.submitButton.isEnabled = true
        viewBinding.belowLayout.visible()
        viewBinding.progressBar.gone()
        viewBinding.confirmBeneficiaryLayout.gone()
        viewBinding.toplayoutblock.visible()
        viewBinding.toplayoutblock.toggleChangeTextView(false)
        viewBinding.toplayoutblock.setVerificationSuccessfulView(
            getString(R.string.bank_account_veri),
            getString(R.string.you_need_to_upload_veri)
        )
        initializeImages()
        viewBinding.toplayoutblock.resetAllViews()

        viewBinding.bankAccNumberItl.editText?.setText("")
        viewBinding.bankNameTil.editText?.setText("")
        viewBinding.ifscCode.editText?.setText("")
    }

    private fun showBankBeneficiaryName(obj: BankDetailsDataModel) {
//        obj.bankBeneficiaryName?.let { beneficiary ->
//            if (beneficiary.isNotBlank()) {
//                viewBinding.toplayoutblock.viewChangeOnStarted()
//                viewBinding.confirmBeneficiaryLayout.visible()
//                viewBinding.belowLayout.gone()
//                viewBinding.submitButton.gone()
//                viewBinding.progressBar.gone()
//                viewBinding.beneficiaryName.text = beneficiary
//                viewBinding.toplayoutblock.setVerificationSuccessfulView(
//                    getString(R.string.bank_verification_pending_veri),
//                    getString(R.string.verifying_veri)
//                )
//                var list = ArrayList<KYCImageModel>()
//                obj.passbookImagePath?.let {
//                    getDBImageUrl(it)?.let {
//                        list.add(
//                            KYCImageModel(
//                                text = getString(R.string.upload_pan_card_new_veri),
//                                imagePath = it,
//                                imageUploaded = true
//                            )
//                        )
//                    }
//                }
////                viewBinding.toplayoutblock.setImageViewPager(list) need to remove uploading option 2856 ticket
//            }
//        }

        obj.bankBeneficiaryName?.let { beneficiary ->
            if (beneficiary.isNotBlank()) {
                viewBinding.scrollView.gone()
                viewBinding.toplayoutblock.gone()
                viewBinding.confirmBeneficiaryLayout.gone()
                viewBinding.belowLayout.gone()
                viewBinding.submitButton.gone()
                viewBinding.progressBar.gone()
                viewBinding.bnConfirmationCl.visible()
                viewBinding.scrollView.gone()
                viewBinding.submitButton.gone()

//                viewBinding.beneficiaryName.text = beneficiary
//                viewBinding.toplayoutblock.setVerificationSuccessfulView(
//                    getString(R.string.bank_verification_pending_veri),
//                    getString(R.string.verifying_veri)
//                )
//                var list = ArrayList<KYCImageModel>()
//                obj.passbookImagePath?.let {
//                    getDBImageUrl(it)?.let {
//                        list.add(
//                            KYCImageModel(
//                                text = getString(R.string.upload_pan_card_new_veri),
//                                imagePath = it,
//                                imageUploaded = true
//                            )
//                        )
//                    }
//                }

                viewBinding.bnTv.text = beneficiary
                viewBinding.accountNoTv.text = obj.accountNo
                viewBinding.ifscTv.text = obj.ifscCode
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

    inner class IFSCCodeTextWatcher : TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            if (text.toString().length == 5 && fifthCharIsNotZero(text.toString())) {
                var str = StringBuilder(text)
                str.setCharAt(4, '0')
                    .also {
                        viewBinding.ifscCode.editText?.setText(str.toString())
                        viewBinding.ifscCode.editText?.setSelection(str.length)
                    }
            }

        }

        private fun fifthCharIsNotZero(text: String): Boolean {
            return text[IFSC_ZERO_TEXT_INDEX_POSITION] != '0'
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    }

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
                viewBinding.toplayoutblock.hideUploadOption(true)
            }

        })
        viewBinding.bankNameTil.editText?.addTextChangedListener(ValidationTextWatcher())
        viewBinding.bankAccNumberItl.editText?.addTextChangedListener(ValidationTextWatcher())
//        viewBinding.ifscCode.editText?.addTextChangedListener(ValidationTextWatcher())
        viewBinding.ifscCode.editText?.addTextChangedListener(IFSCCodeTextWatcher())
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
            viewBinding.submitButton.text = getString(R.string.skip_veri)
        })

        viewBinding.okayButton.setOnClickListener{
            checkForNextDoc()
        }

        viewBinding.submitButton.setOnClickListener {
            hideSoftKeyboard()

            if (viewBinding.toplayoutblock.isDocDontOptChecked() || verificationScreenStatus == VerificationScreenStatus.VERIFIED || verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING || verificationScreenStatus == VerificationScreenStatus.COMPLETED || !anyDataEntered) {
                checkForNextDoc()
            } else {
                val ifsc =
                    viewBinding.ifscCode.editText?.text.toString().toUpperCase(Locale.getDefault())
                if (!VerificationValidations.isIfSCValid(ifsc)) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.enter_valid_ifsc_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (viewBinding.bankNameTil.editText?.text.toString().isNullOrBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.enter_bank_name_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }
                if (viewBinding.bankNameTil.editText?.text.toString().length < 3) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.bank_name_too_short_veri))
                        .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                        .show()
                    return@setOnClickListener
                }

                if (viewBinding.bankAccNumberItl.editText?.text.toString().length < 4) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_veri))
                        .setMessage(getString(R.string.enter_valid_acc_no_veri))
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

        viewBinding.appBarBank.apply {
            changeBackButtonDrawable()
            makeBackgroundMoreRound()
            setBackButtonListener(View.OnClickListener {
//                navigation.popBackStack()
                activity?.onBackPressed()
            })
            user?.let {
                showSubtitle(it.phoneNumber)
            }

        }
        viewBinding.confirmButton.setOnClickListener {
            var props = HashMap<String, Any>()
            props.put("Bank verified", true)
            eventTracker.setUserProperty(props)
            eventTracker.pushEvent(
                TrackingEventArgs(
                    eventName = VerificationEvents.BANK_VERIFIED,
                    props = null
                )
            )
            viewModel.setVerificationStatusInDB(true, userIdToUse.toString())
            viewBinding.toplayoutblock.hideOnVerifiedDocuments()
        }
        viewBinding.notConfirmButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.want_to_reenter_bank_details_veri))
                .setPositiveButton(getString(R.string.yes_veri)) { _, _ ->
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.BANK_MISMATCH,
                            props = null
                        )
                    )
                    viewModel.setVerificationStatusInDB(false, userIdToUse.toString())
//                    viewModel.setVerificationStatusStringToBlank(userIdToUse.toString())
                }
                .setNegativeButton(getString(R.string.no_veri)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        viewBinding.confirmBnBs.setOnClickListener{
            user?.uid?.let {
                var props = HashMap<String, Any>()
                props.put("Bank verified", true)
                eventTracker.setUserProperty(props)
                eventTracker.pushEvent(
                    TrackingEventArgs(
                        eventName = VerificationEvents.BANK_VERIFIED,
                        props = null
                    )
                )
                viewModel.setVerificationStatusInDB(true, it)
            }

        }

        viewBinding.cancelButton.setOnClickListener{
            eventTracker.pushEvent(
                TrackingEventArgs(
                    eventName = VerificationEvents.BANK_MISMATCH,
                    props = null
                )
            )
            viewModel.setVerificationStatusInDB(false, userIdToUse.toString())
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
                allNavigationList.get(0), intentBundle
            )
//            navigation.navigateTo(
//                allNavigationList.get(0),
//                bundleOf(VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle,if(FROM_CLIENT_ACTIVATON) StringConstants.FROM_CLIENT_ACTIVATON.value to true else StringConstants.FROM_CLIENT_ACTIVATON.value to false)
//            )

        }
    }

    private fun initializeImages() {
        // verification_doc_image ic_passbook_illustration
        // need to remove uploading option 2856 ticket
//        val frontUri = Uri.Builder()
//            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
//            .authority(resources.getResourcePackageName(R.drawable.verification_doc_image))
//            .appendPath(resources.getResourceTypeName(R.drawable.verification_doc_image))
//            .appendPath(resources.getResourceEntryName(R.drawable.verification_doc_image))
//            .build()
//        val list =
//            listOf(
//                KYCImageModel(
//                    text = getString(R.string.upload_bank_account_new_veri),
//                    imageIcon = frontUri,
//                    imageUploaded = false
//                )
//            )
        viewBinding.toplayoutblock.initAdapter()
        viewBinding.toplayoutblock.hideUploadOption(true)
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
        image?.let {
            eventTracker.pushEvent(TrackingEventArgs(VerificationEvents.BANK_OCR_STARTED, null))
            viewModel.getKycOcrResult("bank", "dummy", it, userIdToUse.toString())
        }
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            VerificationClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                getString(R.string.upload_passbook_veri),
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
                        getString(R.string.upload_passbook_veri),
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
            val outputFileUri =
                ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
//                startCrop(outputFileUri)
                startCropImage(outputFileUri)
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
        } else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? =
                Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
//                Log.d("ImageUri", imageUriResultCrop.toString())
            clickedImagePath = imageUriResultCrop
            showPassbookInfoCard(clickedImagePath!!)
        }

    }

    private fun startCropImage(imageUri: Uri): Unit {
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_do_we_need_this_bank_veri)
        )
    }

    private fun callKycVerificationApi() {
        var list = listOf(
            Data("name", viewBinding.bankNameTil.editText?.text.toString()),
            Data("no", viewBinding.bankAccNumberItl.editText?.text.toString()),
            Data("ifsccode", viewBinding.ifscCode.editText?.text.toString())
        )
        activeLoader(true)
        var map = mapOf(
            "Account number" to viewBinding.bankAccNumberItl.editText?.text.toString(),
            "IFSC code" to viewBinding.ifscCode.editText?.text.toString(),
            "Bank name" to viewBinding.bankNameTil.editText?.text.toString()
        )
        eventTracker.pushEvent(
            TrackingEventArgs(
                eventName = VerificationEvents.BANK_DETAIL_SUBMITTED,
                props = map
            )
        )
        viewModel.getKycVerificationResult("bank", list, userIdToUse.toString())
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
        options.setToolbarTitle(getString(R.string.crop_and_rotate_veri))
        return options
    }

//    override fun onBackPressed(): Boolean {
//        if (verificationScreenStatus == VerificationScreenStatus.STARTED_VERIFYING) {
//            reContinueDialog()
//            return true
//        } else return false
//
//    }

    private fun reContinueDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.want_to_wait_veri))
            .setPositiveButton(getString(R.string.yes_veri)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no_veri)) { dialog, _ ->
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