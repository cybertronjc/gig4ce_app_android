package com.gigforce.verification.gigerVerfication.bankDetails

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.core.utils.VerificationValidations
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.GigerVerificationStatus
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lse
import com.gigforce.verification.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_bank_details_info_2.*
import kotlinx.android.synthetic.main.fragment_add_bank_details_info_main_2.*
import kotlinx.android.synthetic.main.fragment_add_bank_details_info_view_2.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddBankDetailsInfoFragment : Fragment(), IOnBackPressedOverride {

    companion object {
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
        const val INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT = "user_came_from_amb_screen"
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var bankDetailsDataModel: BankDetailsDataModel? = null
    private var didUserCameFromAmbassadorScreen = false

    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_add_bank_details_info_2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            didUserCameFromAmbassadorScreen =
                it.getBoolean(INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT)
        }

        savedInstanceState?.let {
            didUserCameFromAmbassadorScreen =
                it.getBoolean(INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
            didUserCameFromAmbassadorScreen
        )
    }

    private fun initViews() {
        passbookImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_bank_passbook_veri)
        passbookImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_bank_passbook_sublabel_veri)

        toolbar.apply {
            hideActionMenu()
            showTitle(getString(R.string.giger_verification_veri))
            setBackButtonListener(object : View.OnClickListener{
                override fun onClick(p0: View?) {
                    if (didUserCameFromAmbassadorScreen) {
                        onBackPressed()
                    } else {
                        navigation.popBackStack("verification/main",inclusive = false)
//                    findNavController().popBackStack(R.id.gigerVerificationFragment, false)
                    }
                }
            })
//            setBackButtonListener{
//
//                if (didUserCameFromAmbassadorScreen) {
//                    onBackPressed()
//                } else {
//                    navigation.popBackStack("verification/main",inclusive = false)
////                    findNavController().popBackStack(R.id.gigerVerificationFragment, false)
//                }
//            }
        }

        appBarComp.apply {
            setAppBarTitle(getString(R.string.giger_verification_veri))
            setBackButtonListener(object : View.OnClickListener{
                override fun onClick(v: View?) {
                    if (didUserCameFromAmbassadorScreen) {
                        onBackPressed()
                    } else {
                        navigation.popBackStack("verification/main",inclusive = false)
//                    findNavController().popBackStack(R.id.gigerVerificationFragment, false)
                    }
                }

            })
        }

        helpIconViewIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisViewTV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        helpIconIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        passbookSubmitSliderBtn.isEnabled = false
        passbookImageHolder.uploadDocumentCardView.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.bank_passbook_front_image_veri)

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageLayout()
                showPassbookInfoLayout()

                if (bankDetailsDataConfirmationCB.isChecked
                    && (passbookSubmitSliderBtn.text == getString(R.string.update_veri)
                            || clickedImagePath != null)

                ) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.passbookNoRB) {
                hidePassbookImageAndInfoLayout()

                passbookSubmitSliderBtn.visible()
                bankDetailsDataConfirmationCB.visible()

                if (bankDetailsDataConfirmationCB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else {
                hidePassbookImageAndInfoLayout()
                disableSubmitButton()
            }
        }

        bankDetailsDataConfirmationCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (passbookAvailaibilityOptionRG.checkedRadioButtonId == R.id.passbookNoRB)
                    enableSubmitButton()
                else if (passbookAvailaibilityOptionRG.checkedRadioButtonId == R.id.passbookYesRB &&
                    (passbookSubmitSliderBtn.text == getString(R.string.update_veri) || clickedImagePath != null)
                )
                    enableSubmitButton()
                else
                    disableSubmitButton()

            } else
                disableSubmitButton()
        }

//        editBankDetailsLayout.setOnClickListener {
//            navigate(R.id.editBankDetailsInfoBottomSheet)
//        }

        passbookSubmitSliderBtn.setOnClickListener  {

                    if (passbookYesRB.isChecked || passbookSubmitSliderBtn.text == getString(R.string.update_veri)) {

                        val ifsc = ifscEditText.text.toString().toUpperCase(Locale.getDefault())
                        if (!VerificationValidations.isIfSCValid(ifsc)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.enter_valid_ifsc_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()

                            return@setOnClickListener
                        }

                        if (bankNameEditText.text.isNullOrBlank()) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.enter_bank_name_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()

                            return@setOnClickListener
                        }

                        if (bankNameEditText.text.toString().length < 3) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.bank_name_too_short_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()

                            return@setOnClickListener
                        }

                        if (accountNoEditText.text.toString().length < 4) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.enter_valid_acc_no_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()

                            return@setOnClickListener
                        }

//                        if (passbookSubmitSliderBtn.text != getString(R.string.update) && clickedImagePath == null) {
//
//                            MaterialAlertDialogBuilder(requireContext())
//                                .setTitle(getString(R.string.alert))
//                                .setMessage(getString(R.string.click_or_select_bank_passbook))
//                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
//                                .show()
//                            passbookSubmitSliderBtn.resetSlider()
//                            return
//                        }

                        val accNo = accountNoEditText.text.toString()
                        val bankName =
                            bankNameEditText.text.toString().capitalize()

                        viewModel.updateBankPassbookImagePath(
                            userHasPassBook = true,
                            passbookImagePath = clickedImagePath,
                            ifscCode = ifsc,
                            bankName = bankName,
                            accountNo = accNo
                        )

                    } else if (passbookNoRB.isChecked) {

                        viewModel.updateBankPassbookImagePath(
                            userHasPassBook = false,
                            passbookImagePath = null,
                            ifscCode = null,
                            bankName = null,
                            accountNo = null
                        )
                    }
                }


        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert_veri))
                .setMessage(getString(R.string.your_are_reuploading_bank_details_veri))
                .setPositiveButton(getString(R.string.okay_veri)) { _, _ ->

                    bankViewLayout.gone()
                    bankEditLayout.visible()

                    setDataOnEditLayout(bankDetailsDataModel)
                    passbookAvailaibilityOptionRG.check(R.id.passbookYesRB)
                    passbookSubmitSliderBtn.isEnabled = false
                }
                .setNegativeButton(getString(R.string.cancel_veri)) { _, _ -> }
                .show()
        }

    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_we_need_this_bank_veri)
        )
    }

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                this.gigerVerificationStatus = it
                this.bankDetailsDataModel = it.bankUploadDetailsDataModel
                progressBar.gone()

                if (it.bankDetailsUploaded && it.bankUploadDetailsDataModel != null) {

                    if (it.bankUploadDetailsDataModel!!.userHasPassBook != null) {
                        if (it.bankUploadDetailsDataModel!!.userHasPassBook!!) {
                            setDataOnViewLayout(it)
                        } else {
                            setDataOnEditLayout(null)
                            passbookAvailaibilityOptionRG.check(R.id.passbookNoRB)
                        }
                    } else {
                        //Uncheck both and hide capture layout
                        setDataOnEditLayout(null)
                        passbookAvailaibilityOptionRG.clearCheck()
                        hidePassbookImageAndInfoLayout()
                    }
                } else {

                    setDataOnEditLayout(null)
                    passbookAvailaibilityOptionRG.clearCheck()
                    hidePassbookImageAndInfoLayout()
                }
            })


        viewModel.documentUploadState
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lse.Loading -> showLoadingState()
                    Lse.Success -> documentsUploaded()
                    is Lse.Error -> errorOnUploadingDocuments(it.error)
                }
            })

        viewModel.getVerificationStatus()
    }

    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        bankEditLayout.gone()
        bankViewLayout.visible()
        //TODO handle error message when process is ready

        val bankDetails = gigVerificationStatus.bankUploadDetailsDataModel ?: return

        statusTV.text = bankDetails.verifiedString
        statusTV.setTextColor(
            ResourcesCompat.getColor(
                resources,
                gigVerificationStatus.getColorCodeForStatus(bankDetails.state),
                null
            )
        )


        if (bankDetails.passbookImagePath != null) {

            if (bankDetails.passbookImagePath!!.startsWith("http", true)) {
                Glide.with(requireContext()).load(bankDetails.passbookImagePath)
                    .placeholder(getCircularProgressDrawable()).into(bankViewImageIV)
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(bankDetails.passbookImagePath!!)
                    .downloadUrl.addOnSuccessListener {
                        Glide.with(requireContext())
                            .load(it)
                            .placeholder(getCircularProgressDrawable())
                            .into(bankViewImageIV)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }
        bankViewImageErrorMessage.gone()


        ifscNoTV.text = bankDetails.ifscCode
        ifscErrorMessage.gone()

        bankNameTV.text = bankDetails.bankName
        bankNameErrorMessage.gone()

        bankAccountNoTV.text = bankDetails.accountNo
        bankAccountNoErrorMessage.gone()
    }


    private fun setDataOnEditLayout(it: BankDetailsDataModel?) {
        bankViewLayout.gone()
        bankEditLayout.visible()

        if (it != null) {
            //Fill previous data
            passbookAvailaibilityOptionRG.gone()
            doYouHavePassbookLabel.gone()
        } else {
            passbookAvailaibilityOptionRG.visible()
            doYouHavePassbookLabel.visible()

            bankEditOverallErrorMessage.gone()
            bankIfscEditErrorMessage.gone()
            bankNameEditErrorMessage.gone()
            bankAccNoEditErrorMessage.gone()
            bankImageEditErrorMessage.gone()
        }

        val bankData = it ?: return
        passbookSubmitSliderBtn.text = getString(R.string.update_veri)
        ifscEditText.setText(bankData.ifscCode)
        bankNameEditText.setText(bankData.bankName)
        accountNoEditText.setText(bankData.accountNo)

        if (bankData.passbookImagePath != null) {

            if (bankData.passbookImagePath!!.startsWith("http", true)) {
                showPassbookInfoCard(Uri.parse(bankData.passbookImagePath))
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(bankData.passbookImagePath!!)
                    .downloadUrl.addOnSuccessListener {
                        showPassbookInfoCard(it)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }
    }

    override fun onBackPressed(): Boolean {

        if (didUserCameFromAmbassadorScreen) {
            var navFragmentsData = activity as NavFragmentsData
            navFragmentsData.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            navigation.popBackStack()
            return true
        } else {
            navigation.popBackStack("verification/main",inclusive = false)
//            findNavController().popBackStack(R.id.gigerVerificationFragment, false)
            return true
        }
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        bankViewLayout.visibility = View.GONE
        bankEditLayout.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_veri))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
            .show()
    }

    private fun documentsUploaded() {
        showToast(getString(R.string.bank_details_updated_veri))


        if (didUserCameFromAmbassadorScreen) {
            activity?.onBackPressed()
        } else {
            gigerVerificationStatus?.let {

                /*if (!it.selfieVideoUploaded) {
                    navigation.navigateTo("verification/addSelfieVideoFragment")
                } else*/ if (!it.panCardDetailsUploaded) {
                    navigation.navigateTo("verification/addPanCardInfoFragment")
                } else if (!it.aadharCardDetailsUploaded) {
                    navigation.navigateTo("verification/addAadharCardInfoFragment")
                } else if (!it.dlCardDetailsUploaded) {
                    navigation.navigateTo("verification/addDrivingLicenseInfoFragment")
                } else {
                    showDetailsUploaded()
                }
            }
        }
    }

    private fun showDetailsUploaded() {
        val view =
            layoutInflater.inflate(R.layout.fragment_giger_verification_documents_submitted, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .show()

        view.findViewById<View>(R.id.verificationCompletedBtn)
            .setOnClickListener {
                dialog.dismiss()
                navigation.popBackStack("verification/main",inclusive = false)
//                findNavController().popBackStack(R.id.gigerVerificationFragment, false)
            }
    }


    private fun showLoadingState() {
        bankViewLayout.visibility = View.GONE
        bankEditLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }


    private fun showCameraAndGalleryOption() {
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
            "purpose",
            "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "pan_card.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,REQUEST_CODE_CAPTURE_BANK_PHOTO,requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_CAPTURE_BANK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAPTURE_BANK_PHOTO) {

            if (resultCode == Activity.RESULT_OK) {
                clickedImagePath =
                    data?.getParcelableExtra("uri")
                showPassbookInfoCard(clickedImagePath!!)

                if (bankDetailsDataConfirmationCB.isChecked)
                    enableSubmitButton()

                if (clickedImagePath != null && passbookSubmitSliderBtn.isGone) {
                    bankDetailsDataConfirmationCB.visible()
                    passbookSubmitSliderBtn.visible()
                }

            }

//            else {
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle(getString(R.string.alert))
//                    .setMessage(getString(R.string.unable_to_capture_image))
//                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
//                    .show()
//            }
        }
    }

    private fun disableSubmitButton() {
        passbookSubmitSliderBtn.isEnabled = false

    }

    private fun showPassbookImageLayout() {
        passbookImageHolder.visibility = View.VISIBLE
    }

    private fun showPassbookInfoLayout() {
        passbookInfoLayout.visibility = View.VISIBLE
    }

    private fun hidePassbookImageAndInfoLayout() {
        passbookImageHolder.visibility = View.GONE
        passbookInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        passbookSubmitSliderBtn.isEnabled = true
    }


    private fun showPassbookInfoCard(panInfoPath: Uri) {
        passbookImageHolder.uploadDocumentCardView.visibility = View.GONE
        passbookImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(panInfoPath)
            .placeholder(getCircularProgressDrawable())
            .into(passbookImageHolder.uploadImageLayout.clickedImageIV)
    }


}