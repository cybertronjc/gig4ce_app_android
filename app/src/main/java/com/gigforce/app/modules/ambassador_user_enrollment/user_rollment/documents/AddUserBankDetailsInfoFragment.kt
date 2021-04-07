package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.documents

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.VerificationValidations
import com.gigforce.app.modules.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.app.modules.gigerVerfication.bankDetails.BankDetailsDataModel
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_ambsd_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_bank_details_info_main.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_bank_details_info_view.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.util.*

@ExperimentalStdlibApi
class AddUserBankDetailsInfoFragment : BaseFragment() {

    companion object {
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private lateinit var userId: String
    private lateinit var userName: String
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var bankDetailsDataModel: BankDetailsDataModel? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_add_bank_details_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initViews()
        initViewModel()
        getUserDetails()
    }

    private fun getUserDetails() {
        viewModel.getVerificationStatus(userId)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
    }

    private fun initViews() {
        passbookImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_bank_passbook)
        passbookImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_bank_passbook_sublabel)

        toolbar_layout.apply {
            showTitle(getString(R.string.upload_bank_details))
            hideActionMenu()
            setBackButtonListener {
                showGoBackConfirmationDialog()
            }
        }

        helpIconIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        disableSubmitButton()
        passbookImageHolder.uploadDocumentCardView.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.bank_passbook_front_image)

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageLayout()
                showPassbookInfoLayout()
                enableSubmitButton()

            } else if (checkedId == R.id.passbookNoRB) {
                hidePassbookImageAndInfoLayout()

                passbookSubmitSliderBtn.visible()
                enableSubmitButton()
            } else {
                hidePassbookImageAndInfoLayout()
                disableSubmitButton()
            }
        }


        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.your_are_reuploading_bank_details))
                    .setPositiveButton(getString(R.string.okay)) { _, _ ->

                        bankViewLayout.gone()
                        bankEditLayout.visible()

                        setDataOnEditLayout(bankDetailsDataModel)
                        passbookAvailaibilityOptionRG.check(R.id.passbookYesRB)
                       enableSubmitButton()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                    .show()
        }

        ambsd_bank_skip_btn.setOnClickListener {

            navigate(
                    R.id.addUserPanCardInfoFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            )
        }

//        editBankDetailsLayout.setOnClickListener {
//            navigate(R.id.editBankDetailsInfoBottomSheet)
//        }

        passbookSubmitSliderBtn.setOnClickListener {

            if (passbookYesRB.isChecked || passbookSubmitSliderBtn.text == getString(R.string.update)) {

                val ifsc = ifscEditText.text.toString().toUpperCase(Locale.getDefault())
                if (!VerificationValidations.isIfSCValid(ifsc)) {

                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.enter_valid_ifsc))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()

                    return@setOnClickListener
                }

//                        if (bankNameEditText.text.isNullOrBlank()) {
//
//                            MaterialAlertDialogBuilder(requireContext())
//                                .setTitle(getString(R.string.alert))
//                                .setMessage(getString(R.string.enter_bank_name))
//                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
//                                .show()
//
//                            passbookSubmitSliderBtn.resetSlider()
//                            return
//                        }

//                        if (bankNameEditText.text.toString().length < 3) {
//
//                            MaterialAlertDialogBuilder(requireContext())
//                                .setTitle(getString(R.string.alert))
//                                .setMessage(getString(R.string.bank_name_too_short))
//                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
//                                .show()
//
//                            passbookSubmitSliderBtn.resetSlider()
//                            return
//                        }

                if (accountNoEditText.text.toString().length < 4) {

                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.enter_valid_acc_no))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
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
                        bankNameEditText.text.toString().capitalize(Locale.getDefault())

                viewModel.updateBankPassbookImagePath(
                        userHasPassBook = true,
                        passbookImagePath = clickedImagePath,
                        ifscCode = ifsc,
                        bankName = bankName,
                        accountNo = accNo,
                        userId = userId
                )

            } else if (passbookNoRB.isChecked) {

                viewModel.updateBankPassbookImagePath(
                        userHasPassBook = false,
                        passbookImagePath = null,
                        ifscCode = null,
                        bankName = null,
                        accountNo = null,
                        userId = userId
                )
            }

        }

        ambsd_passbook_edit_skip_btn.setOnClickListener {

            navigate(
                    R.id.addUserPanCardInfoFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            )
        }
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                title = getString(R.string.why_do_we_need_this),
                content = getString(R.string.why_we_need_this_bank)
        )
    }

    private fun initViewModel() {

        viewModel.gigerVerificationStatus
                .observe(viewLifecycleOwner, Observer {
                    this.gigerVerificationStatus = it
                    this.bankDetailsDataModel = it.bankUploadDetailsDataModel
                    progressBar.gone()

                    if (it.bankDetailsUploaded && it.bankUploadDetailsDataModel != null) {

                        if (it.bankUploadDetailsDataModel.userHasPassBook != null) {
                            if (it.bankUploadDetailsDataModel.userHasPassBook) {
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
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        bankViewLayout.visibility = View.GONE
        bankEditLayout.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(error)
                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                .show()
    }

    private fun documentsUploaded() {

        if (passbookYesRB.isChecked)
            showToast(getString(R.string.bank_details_uploaded))

        navigate(
                R.id.addUserPanCardInfoFragment, bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
        )
        )
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
//        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
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

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
                PhotoCrop.INTENT_EXTRA_PURPOSE,
                PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "pan_card.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_CAPTURE_BANK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAPTURE_BANK_PHOTO) {

            if (resultCode == Activity.RESULT_OK) {
                clickedImagePath =
                        data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                showPassbookInfoCard(clickedImagePath!!)
                enableSubmitButton()

            } else {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.unable_to_capture_image))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
            }
        }
    }

    private fun disableSubmitButton() {
        passbookSubmitSliderBtn.isEnabled = false

        passbookSubmitSliderBtn.strokeColor = ColorStateList.valueOf(
                ResourcesCompat.getColor(resources, R.color.light_grey, null)
        )
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

        passbookSubmitSliderBtn.strokeColor = ColorStateList.valueOf(
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
        )
    }


    private fun showPassbookInfoCard(panInfoPath: Uri) {
        passbookImageHolder.uploadDocumentCardView.visibility = View.GONE
        passbookImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
                .load(panInfoPath)
                .placeholder(getCircularProgressDrawable())
                .into(passbookImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        bankEditLayout.gone()
        bankViewLayout.visible()

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

            if (bankDetails.passbookImagePath.startsWith("http", true)) {
                Glide.with(requireContext()).load(bankDetails.passbookImagePath)
                        .placeholder(getCircularProgressDrawable()).into(bankViewImageIV)
            } else {
                firebaseStorage
                        .reference
                        .child("verification")
                        .child(bankDetails.passbookImagePath)
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
        passbookSubmitSliderBtn.text = getString(R.string.update)
        ifscEditText.setText(bankData.ifscCode)
        bankNameEditText.setText(bankData.bankName)
        accountNoEditText.setText(bankData.accountNo)

        if (bankData.passbookImagePath != null) {

            if (bankData.passbookImagePath.startsWith("http", true)) {
                showPassbookInfoCard(Uri.parse(bankData.passbookImagePath))
            } else {
                firebaseStorage
                        .reference
                        .child("verification")
                        .child(bankData.passbookImagePath)
                        .downloadUrl.addOnSuccessListener {
                            showPassbookInfoCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
            }
        }
    }


}