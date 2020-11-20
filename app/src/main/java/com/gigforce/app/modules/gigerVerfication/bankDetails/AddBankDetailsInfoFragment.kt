package com.gigforce.app.modules.gigerVerfication.bankDetails

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.VerificationValidations
import com.gigforce.app.modules.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_add_bank_details_info_main.*
import kotlinx.android.synthetic.main.fragment_add_bank_details_info_view.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.util.*

@ExperimentalStdlibApi
class AddBankDetailsInfoFragment : BaseFragment() {

    companion object {
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var bankDetailsDataModel: BankDetailsDataModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_bank_details_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViews() {
        passbookImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_bank_passbook)
        passbookImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_bank_passbook_sublabel)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack(R.id.gigerVerificationFragment, false)
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
            getString(R.string.bank_passbook_front_image)

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->
            passbookSubmitSliderBtn.resetSlider()

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageLayout()
                showPassbookInfoLayout()

                if (bankDetailsDataConfirmationCB.isChecked
                    && (passbookSubmitSliderBtn.text == getString(R.string.update)
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
                     (passbookSubmitSliderBtn.text == getString(R.string.update) || clickedImagePath != null)
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

        passbookSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {

                    if (passbookYesRB.isChecked || passbookSubmitSliderBtn.text == getString(R.string.update)) {

                        val ifsc = ifscEditText.text.toString().toUpperCase(Locale.getDefault())
                        if (!VerificationValidations.isIfSCValid(ifsc)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.enter_valid_ifsc))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()

                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (bankNameEditText.text.isNullOrBlank()) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.enter_bank_name))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()

                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (bankNameEditText.text.toString().length < 3) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.bank_name_too_short))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()

                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (accountNoEditText.text.toString().length < 4) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.enter_valid_acc_no))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()

                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (passbookSubmitSliderBtn.text != getString(R.string.update) && clickedImagePath == null) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.click_or_select_bank_passbook))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()
                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        val accNo = accountNoEditText.text.toString()
                        val bankName =
                            bankNameEditText.text.toString().capitalize(Locale.getDefault())

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
                    passbookSubmitSliderBtn.isEnabled = true
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                .show()
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

    override fun onBackPressed(): Boolean {
        findNavController().popBackStack(R.id.gigerVerificationFragment, false)
        return true
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        bankViewLayout.visibility = View.GONE
        bankEditLayout.visibility = View.VISIBLE
        passbookSubmitSliderBtn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
            .show()
    }

    private fun documentsUploaded() {
        showToast(getString(R.string.bank_details_uploaded))

        gigerVerificationStatus?.let {

            if (!it.selfieVideoUploaded) {
                navigate(R.id.addSelfieVideoFragment)
            } else if (!it.panCardDetailsUploaded) {
                navigate(R.id.addPanCardInfoFragment)
            } else if (!it.aadharCardDetailsUploaded) {
                navigate(R.id.addAadharCardInfoFragment)
            } else if (!it.dlCardDetailsUploaded) {
                navigate(R.id.addDrivingLicenseInfoFragment)
            } else {
                showDetailsUploaded()
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
                findNavController().popBackStack(R.id.gigerVerificationFragment, false)
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

        passbookSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        passbookSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
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

        passbookSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        passbookSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
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