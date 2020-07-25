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
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_add_bank_details_info_main.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

class AddBankDetailsInfoFragment : BaseFragment() {

    companion object {
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path"
        const val INTENT_EXTRA_IFSC = "ifsc"
        const val INTENT_EXTRA_ACC_NO = "acc_no"
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var gigerVerificationStatus: GigerVerificationStatus? = null


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

        passbookSubmitSliderBtn.isEnabled = false
        passbookImageHolder.uploadDocumentCardView.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.imageLabelTV.text =
            "Bank Passbook (Front Page)"

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->
            passbookSubmitSliderBtn.resetSlider()

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageLayout()
                showPassbookInfoLayout()

                if (bankDetailsDataConfirmationCB.isChecked && clickedImagePath != null) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.passbookNoRB && bankDetailsDataConfirmationCB.isChecked) {
                hidePassbookImageAndInfoLayout()
                enableSubmitButton()
            } else {
                hidePassbookImageAndInfoLayout()
                disableSubmitButton()
            }
        }

        bankDetailsDataConfirmationCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (passbookAvailaibilityOptionRG.checkedRadioButtonId == R.id.passbookNoRB)
                    enableSubmitButton()
                else if (passbookAvailaibilityOptionRG.checkedRadioButtonId == R.id.passbookYesRB && clickedImagePath != null)
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

                    if (passbookNoRB.isChecked) {

                        viewModel.updateBankPassbookImagePath(
                            userHasPassBook = false,
                            passbookImagePath = null,
                            ifscCode = null,
                            accountNo = null
                        )
                    } else {

                        if (ifscEditText.text!!.length != 11) {
                            ifscTextInputLayout.error = "Enter Valid IfSC Code"
                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (accountNoEditText.text.isNullOrBlank()) {
                            accountNoTextInputLayout.error = "Enter Account No"
                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (clickedImagePath == null) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Alert")
                                .setMessage("Click Or Select your Passbook Image first")
                                .setPositiveButton("OK") { _, _ -> }
                                .show()
                            passbookSubmitSliderBtn.resetSlider()
                            return
                        }

                        val ifsc = ifscEditText.text.toString()
                        val accNo = accountNoEditText.text.toString()

                        if (passbookSubmitSliderBtn.text.toString() == getString(R.string.update)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Alert")
                                .setMessage("You are re-uploading your Bank details, they will be verified once again, that can take up to 7 days")
                                .setPositiveButton("OK") { _, _ ->

                                    viewModel.updateBankPassbookImagePath(
                                        userHasPassBook = true,
                                        passbookImagePath = clickedImagePath,
                                        ifscCode = ifsc,
                                        accountNo = accNo
                                    )
                                }
                                .show()

                        } else {

                            viewModel.updateBankPassbookImagePath(
                                userHasPassBook = true,
                                passbookImagePath = clickedImagePath,
                                ifscCode = ifsc,
                                accountNo = accNo
                            )
                        }
                    }
                }
            }
    }

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                this.gigerVerificationStatus = it
                if (it.bankDetailsUploaded && it.bankUploadDetailsDataModel != null) {

                    if (it.bankUploadDetailsDataModel.userHasPassBook != null) {
                        if (it.bankUploadDetailsDataModel.userHasPassBook) {
                            passbookSubmitSliderBtn.text = getString(R.string.update)
                            bankDetailsDataConfirmationCB.gone()
                            passbookSubmitSliderBtn.gone()

                            passbookAvailaibilityOptionRG.check(R.id.passbookYesRB)
                            ifscEditText.setText(it.bankUploadDetailsDataModel.ifscCode)
                            accountNoEditText.setText(it.bankUploadDetailsDataModel.accountNo)
                        } else
                            passbookAvailaibilityOptionRG.check(R.id.passbookNoRB)
                    } else {
                        //Uncheck both and hide capture layout
                        passbookAvailaibilityOptionRG.clearCheck()
                        passbookImageHolder.visibility = View.GONE
                    }

                    if (it.bankUploadDetailsDataModel.passbookImagePath != null) {
                        val imageRef = firebaseStorage
                            .reference
                            .child("verification")
                            .child(it.bankUploadDetailsDataModel.passbookImagePath)

                        if (bankDetailsDataConfirmationCB.isChecked)
                            passbookSubmitSliderBtn.isEnabled = true

                        imageRef.downloadUrl.addOnSuccessListener {
                            showPassbookInfoCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
                    }
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

    override fun onBackPressed(): Boolean {

        findNavController().popBackStack(R.id.gigerVerificationFragment, false)
        return true
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        bankDetailsMainLayout.visibility = View.VISIBLE
        passbookSubmitSliderBtn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage(error)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun documentsUploaded() {
        showToast("Bank details Uploaded")

        gigerVerificationStatus?.let {

            if (!it.selfieVideoUploaded) {
                navigate(R.id.addSelfieVideoFragment)
            } else if (!it.panCardDetailsUploaded) {
                navigate(R.id.addPanCardInfoFragment)
            } else if (!it.aadharCardDetailsUploaded) {
                navigate(R.id.addDrivingLicenseInfoFragment)
            } else if (!it.dlCardDetailsUploaded) {
                navigate(R.id.addDrivingLicenseInfoFragment)
            } else   {
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
        bankDetailsMainLayout.visibility = View.GONE
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

                if (clickedImagePath != null && passbookSubmitSliderBtn.isGone) {
                    bankDetailsDataConfirmationCB.visible()
                    passbookSubmitSliderBtn.visible()
                }

            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Unable to Capture Image")
                    .setPositiveButton("OK") { _, _ -> }
                    .show()
            }
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
            .into(passbookImageHolder.uploadImageLayout.clickedImageIV)
    }


}