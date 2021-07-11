package com.gigforce.verification.mainverification.bankaccount

import android.Manifest
import android.app.Activity
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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.VerificationValidations
import com.gigforce.verification.R
import com.gigforce.verification.databinding.BankAccountFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
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
                if (it.status) {
                    if (!it.beneficiaryName.isNullOrBlank() || !it.accountNumber.isNullOrBlank() || !it.ifscCode.isNullOrBlank() || !it.bankName.isNullOrBlank()) {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UPLOAD_SUCCESS,
                            "UPLOAD SUCCESSFUL",
                            "Information of Bank Captured Successfully."
                        )
                        viewBinding.bankAccNumberItl.editText?.setText(it.accountNumber)
                        viewBinding.ifscCode.editText?.setText(it.ifscCode)
                        viewBinding.bankNameTil.editText?.setText(it.bankName)
                    } else {
                        viewBinding.toplayoutblock.uploadStatusLayout(
                            AppConstants.UNABLE_TO_FETCH_DETAILS,
                            "UNABLE TO FETCH DETAILS",
                            "Enter your Bank details manually or try again to continue the verification process."
                        )

                    }
                } else
                    showToast("Ocr status " + it.message)
            }
        })

        viewModel.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it.status) {
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.setVerificationSuccessfulView("Verifying")
                    viewModel.getBeneficiaryName()
                    viewBinding.submitButton.gone()
                    viewBinding.toplayoutblock.disableImageClick()
//                    viewBinding.accountHolderName.editText?.setText(it.beneficiaryName)
//                    viewBinding.bankAccNumberItl.editText?.setText(it.accountNumber)
//                    viewBinding.ifscCode.editText?.setText(it.ifscCode)
//                    viewBinding.bankNameTil.editText?.setText(it.bankName)
                } else
                    showToast("Ocr status " + it.message)
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
                        "The Bank Details have been verified successfully."
                    )
                    viewBinding.submitButton.gone()
                    viewBinding.toplayoutblock.setVerificationSuccessfulView()
                }
            }
        })


        viewModel.beneficiaryName.observe(viewLifecycleOwner, Observer {
            //observing beneficiary name here
            it.let {
                if (it.isNotEmpty()) {
                    viewBinding.confirmBeneficiaryLayout.visible()
                    viewBinding.belowLayout.gone()
                    viewBinding.beneficiaryName.setText(it)
                } else { showToast("Empty") }
            }
        })
        viewModel.verifiedStatus.observe(viewLifecycleOwner, Observer {
            //verified entry to firebase
            //showToast("Verified")
            it.let {
                if (it){
                    viewBinding.belowLayout.gone()
                    viewBinding.toplayoutblock.uploadStatusLayout(
                        AppConstants.UPLOAD_SUCCESS,
                        "VERIFICATION COMPLETED",
                        "The Bank Details have been verified successfully."
                    )
                    viewBinding.submitButton.gone()
                    viewBinding.toplayoutblock.setVerificationSuccessfulView()
                }
            }

        })
        viewModel.verifiedStatusDB.observe(viewLifecycleOwner, Observer {
            viewBinding.toplayoutblock.setVerificationSuccessfulView("Verified")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Congratulations, your Bank details are verified.")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.okay)) { dialog, _ ->
                    dialog.dismiss()
                    navigation.popBackStack()
                }
                .show()
        })
    }

    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            //showCameraAndGalleryOption()
            checkForPermissionElseShowCameraGalleryBottomSheet()
        })
        viewBinding.submitButton.setOnClickListener {
            hideSoftKeyboard()
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

        viewBinding.toplayoutblock.querytext.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.imageView7.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        viewBinding.appBarBank.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
        viewBinding.confirmButton.setOnClickListener {
            viewModel.setVerificationStatusInDB(true)
        }
        viewBinding.notConfirmButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Do you want to re-enter Bank details?")
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        navigation.popBackStack()
                        navigation.navigateTo("verification/bank_account_fragment")
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                }
                .show()
        }
    }

    private fun setViews() {
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_passbook_illustration))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_passbook_illustration))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_passbook_illustration))
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
                RequestBody.create(MediaType.parse("image/png"), file)
            // MultipartBody.Part is used to send also the actual file name
            image =
                MultipartBody.Part.createFormData("file", file.name, requestFile)
        }
        image?.let { viewModel.getKycOcrResult("bank", "dummy", it) }
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
            Data("name", viewBinding.bankNameTil.editText?.text.toString()),
            Data("no", viewBinding.bankAccNumberItl.editText?.text.toString()),
            Data("ifsccode", viewBinding.ifscCode.editText?.text.toString())
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