package com.gigforce.app.modules.gigerVerfication.drivingLicense

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
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.selectItemWithText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.VerificationValidations
import com.gigforce.app.modules.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_add_driving_license_info_main.*
import kotlinx.android.synthetic.main.fragment_add_driving_license_info_main.whyWeNeedThisTV
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.util.*

enum class DrivingLicenseSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddDrivingLicenseInfoFragment : BaseFragment() {

    companion object {

        const val REQUEST_CODE_UPLOAD_DL = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_FRONT = "front_image"
        const val INTENT_EXTRA_CLICKED_IMAGE_BACK = "back_image"
        const val INTENT_EXTRA_STATE = "state"
        const val INTENT_EXTRA_DL_NO = "dl_no"
    }

    private val viewModel: GigVerificationViewModel by viewModels()

    private var dlFrontImagePath: Uri? = null
    private var dlBackImagePath: Uri? = null
    private var drivingLicenseDetail: DrivingLicenseDataModel? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null
    private var gigerVerificationStatus: GigerVerificationStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_driving_license_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViews() {
        dlFrontImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_driving_license_front_side)
        dlFrontImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_driving_license)

        dlBackImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_driving_license_back_side)
        dlBackImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_driving_license)
        dlSubmitSliderBtn.isEnabled = false

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack(R.id.gigerVerificationFragment, false)
        }

        whyWeNeedThisTV.setOnClickListener {

            WhyWeNeedThisBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                title = "Why we need this?",
                content = "Uploading either Driver’s license or Aadhar card is mandatory for profile verification. A Driver’s license helps verify your name, date of birth, address, and other details."
            )
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()

                if (drivingLicenseDetail?.userHasDL != null &&
                    drivingLicenseDetail?.userHasDL!! &&
                    (dlFrontImagePath == null || dlBackImagePath == null)
                ) {
                    dlSubmitSliderBtn.gone()
                    confirmDLDataCB.gone()
                }

                if (confirmDLDataCB.isChecked
                    && dlFrontImagePath != null
                    && dlBackImagePath != null
                ) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.dlNoRB) {
                hideDLImageAndInfoLayout()

                dlSubmitSliderBtn.visible()
                confirmDLDataCB.visible()

                if (confirmDLDataCB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()

            } else {
                hideDLImageAndInfoLayout()
                disableSubmitButton()
            }
        }

        confirmDLDataCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (dlYesRB.isChecked
                    && dlFrontImagePath != null
                    && dlBackImagePath != null
                )
                    enableSubmitButton()
                else if (dlNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
        }

        dlSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    if (dlYesRB.isChecked) {

                        if (stateSpinner.selectedItemPosition == 0) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Alert")
                                .setMessage("Select Driving License State")
                                .setPositiveButton("OK") { _, _ -> }
                                .show()
                            dlSubmitSliderBtn.resetSlider()
                            return
                        }

                        val dlNo = drivingLicenseEditText.text.toString().toUpperCase(Locale.getDefault())
                        if (!VerificationValidations.isDLNumberValid(dlNo)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Alert")
                                .setMessage("Enter Valid Driving License")
                                .setPositiveButton("OK") { _, _ -> }
                                .show()

                            dlSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (dlFrontImagePath == null || dlBackImagePath == null) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Alert")
                                .setMessage("Select or capture both sides of Driving License")
                                .setPositiveButton("OK") { _, _ -> }
                                .show()
                            dlSubmitSliderBtn.resetSlider()
                            return
                        }

                        val state = stateSpinner.selectedItem.toString()

                        if (dlSubmitSliderBtn.text.toString() == getString(R.string.update)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Alert")
                                .setMessage("You are re-uploading your Driving License details, they will be verified once again, that can take up to 7 days")
                                .setPositiveButton("OK") { _, _ ->

                                    viewModel.updateDLData(
                                        true,
                                        dlFrontImagePath,
                                        dlBackImagePath,
                                        state,
                                        dlNo
                                    )
                                }
                                .show()

                        } else {

                            viewModel.updateDLData(
                                true,
                                dlFrontImagePath,
                                dlBackImagePath,
                                state,
                                dlNo
                            )
                        }
                    } else if (dlNoRB.isChecked) {
                        viewModel.updateDLData(
                            false,
                            null,
                            null,
                            null,
                            null
                        )
                    }
                }
            }

//        editDrivingLicenseInfoLayout.setOnClickListener {
//            navigate(R.id.editDrivingLicenseInfoBottomSheet,Bundle().apply {  })
//        }

        dlFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        dlBackImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }

        dlFrontImageHolder.uploadImageLayout.imageLabelTV.text =
            "Driving License (Front Side)"

        dlBackImageHolder.uploadImageLayout.imageLabelTV.text =
            "Driving License (Back Side)"

        dlFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        dlBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }
    }

    override fun onBackPressed(): Boolean {

        findNavController().popBackStack(R.id.gigerVerificationFragment, false)
        return true
    }

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                this.gigerVerificationStatus = it
                if (it.dlCardDetailsUploaded && it.drivingLicenseDataModel != null) {
                    this.drivingLicenseDetail = it.drivingLicenseDataModel

                    if (it.drivingLicenseDataModel.userHasDL != null) {
                        if (it.drivingLicenseDataModel.userHasDL) {
                            dlSubmitSliderBtn.text = getString(R.string.update)
                            dlSubmitSliderBtn.gone()
                            confirmDLDataCB.gone()

                            //stateAutoCompleteTV.setText(it.drivingLicenseDataModel.dlState)
                            drivingLicenseEditText.setText(it.drivingLicenseDataModel.dlNo)

                            if(it.drivingLicenseDataModel.dlState != null)
                            stateSpinner.selectItemWithText(it.drivingLicenseDataModel.dlState)

                            dlAvailaibilityOptionRG.check(R.id.dlYesRB)
                        } else
                            dlAvailaibilityOptionRG.check(R.id.dlNoRB)
                    } else {
                        //Uncheck both and hide capture layout
                        dlAvailaibilityOptionRG.clearCheck()
                        hideDLImageAndInfoLayout()
                    }

                    if (it.drivingLicenseDataModel.frontImage != null) {
                        val imageRef = firebaseStorage
                            .reference
                            .child("verification")
                            .child(it.drivingLicenseDataModel.frontImage)

                        imageRef.downloadUrl.addOnSuccessListener {
                            showFrontDrivingLicense(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
                    }

                    if (it.drivingLicenseDataModel.backImage != null) {
                        val imageRef = firebaseStorage
                            .reference
                            .child("verification")
                            .child(it.drivingLicenseDataModel.backImage)

                        imageRef.downloadUrl.addOnSuccessListener {
                            showBackDrivingLicense(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
                    }

                    if (confirmDLDataCB.isChecked && it.drivingLicenseDataModel.frontImage != null && it.drivingLicenseDataModel.backImage != null) {
                        enableSubmitButton()
                    }
                }

            })

        viewModel.documentUploadState
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lse.Loading -> showLoadingState()
                    Lse.Success -> documentUploaded()
                    is Lse.Error -> errorOnUploadingDocuments(it.error)
                }
            })

        viewModel.getVerificationStatus()
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        dlMainLayout.visibility = View.VISIBLE
        dlSubmitSliderBtn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage(error)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun documentUploaded() {
        showToast("Driving License Details Uploaded")
        gigerVerificationStatus?.let {

            if (!it.bankDetailsUploaded) {
                navigate(R.id.addBankDetailsInfoFragment)
            } else if (!it.selfieVideoUploaded) {
                navigate(R.id.addSelfieVideoFragment)
            } else if (!it.panCardDetailsUploaded) {
                navigate(R.id.addPanCardInfoFragment)
            } else if (!it.aadharCardDetailsUploaded) {
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
        dlMainLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
        startActivityForResult(
            photoCropIntent,
            REQUEST_CODE_UPLOAD_DL
        )

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_back.jpg")
        startActivityForResult(
            photoCropIntent,
            REQUEST_CODE_UPLOAD_DL
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {

                if (DrivingLicenseSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    dlFrontImagePath =
                        data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                    showFrontDrivingLicense(dlFrontImagePath!!)
                } else if (DrivingLicenseSides.BACK_SIDE == currentlyClickingImageOfSide) {
                    dlBackImagePath =
                        data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                    showBackDrivingLicense(dlBackImagePath!!)
                }

                if (confirmDLDataCB.isChecked
                    && dlFrontImagePath != null
                    && dlBackImagePath != null
                ) {
                    enableSubmitButton()
                }

                if (dlFrontImagePath != null && dlBackImagePath != null && dlSubmitSliderBtn.isGone) {
                    dlSubmitSliderBtn.visible()
                    confirmDLDataCB.visible()
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

    private fun showDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.VISIBLE
        dlFrontImageHolder.visibility = View.VISIBLE
        showImageInfoLayout()
    }

    private fun hideDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.GONE
        dlFrontImageHolder.visibility = View.GONE
        dlInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        dlSubmitSliderBtn.isEnabled = true

        dlSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        dlSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    private fun disableSubmitButton() {
        dlSubmitSliderBtn.isEnabled = false

        dlSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        dlSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
    }

    private fun showImageInfoLayout() {
        dlInfoLayout.visibility = View.VISIBLE
    }

    private fun showFrontDrivingLicense(aadharFrontImagePath: Uri) {
        dlFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
        dlFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(aadharFrontImagePath)
            .into(dlFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun showBackDrivingLicense(aadharBackImagePath: Uri) {
        dlBackImageHolder.uploadDocumentCardView.visibility = View.GONE
        dlBackImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(aadharBackImagePath)
            .into(dlBackImageHolder.uploadImageLayout.clickedImageIV)
    }

}