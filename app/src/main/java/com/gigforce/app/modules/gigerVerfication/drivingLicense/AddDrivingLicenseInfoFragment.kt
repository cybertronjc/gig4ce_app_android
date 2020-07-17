package com.gigforce.app.modules.gigerVerfication.drivingLicense

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_add_driving_license_info_main.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

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
        const val CAME_FROM_AADHAR_SCREEN = "came_from_aadhar"
    }

    private val viewModel: GigVerificationViewModel by viewModels()

    private var dlFrontImagePath: Uri? = null
    private var dlBackImagePath: Uri? = null
    private var drivingLicenseDetail: DrivingLicenseDataModel? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null

    private var cameFromAadharScreen: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_driving_license_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()

        arguments?.let {
            cameFromAadharScreen = it.getBoolean(CAME_FROM_AADHAR_SCREEN)
        }

        savedInstanceState?.let {
            cameFromAadharScreen = it.getBoolean(CAME_FROM_AADHAR_SCREEN)

//            dlFrontImagePath = it.getParcelable(INTENT_EXTRA_CLICKED_IMAGE_FRONT)
//            if (dlFrontImagePath != null) showFrontDrivingLicense(dlFrontImagePath!!)
//
//            dlBackImagePath = it.getParcelable(INTENT_EXTRA_CLICKED_IMAGE_BACK)
//            if (dlBackImagePath != null) showBackDrivingLicense(dlBackImagePath!!)
//
//            drivingLicenseEditText.setText(it.getString(INTENT_EXTRA_DL_NO))
//
//            val index = it.getInt(INTENT_EXTRA_STATE)
//            if (index > 0)
//                stateSpinner.setSelection(index, true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CAME_FROM_AADHAR_SCREEN, cameFromAadharScreen)
//        outState.putParcelable(INTENT_EXTRA_CLICKED_IMAGE_FRONT, dlFrontImagePath)
//        outState.putParcelable(INTENT_EXTRA_CLICKED_IMAGE_BACK, dlBackImagePath)
//        outState.putString(INTENT_EXTRA_DL_NO, drivingLicenseEditText.text.toString())
//        outState.putInt(INTENT_EXTRA_STATE, stateSpinner.selectedItemPosition)
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
            if (cameFromAadharScreen)
                findNavController().popBackStack(R.id.gigerVerificationFragment, false)
            else
                activity?.onBackPressed()
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()

                if (confirmDLDataCB.isChecked
                    && dlFrontImagePath != null
                    && dlBackImagePath != null
                ) {
                    enableSubmitButton()
                }

            } else if (checkedId == R.id.dlNoRB) {
                hideDLImageAndInfoLayout()

                if (confirmDLDataCB.isChecked)
                    enableSubmitButton()
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

        drivingLicenseEditText.doOnTextChanged { text, start, count, after ->
            drivingLicenseTextInputLayout.error = null
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

                        if (drivingLicenseEditText.text!!.length != 15) {
                            drivingLicenseTextInputLayout.error = "Enter Valid Driving License"
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

                        val dlNo = drivingLicenseEditText.text.toString()
                        val state = stateSpinner.selectedItem.toString()
                        viewModel.updateDLData(
                            true,
                            dlFrontImagePath,
                            dlBackImagePath,
                            state,
                            dlNo
                        )
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

        if (cameFromAadharScreen) {
            findNavController().popBackStack(R.id.gigerVerificationFragment, false)
            return true
        } else {
            return super.onBackPressed()
        }
    }

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if (it.dlCardDetailsUploaded && it.drivingLicenseDataModel != null) {
                    this.drivingLicenseDetail = it.drivingLicenseDataModel

                    if (it.drivingLicenseDataModel.userHasDL != null) {
                        if (it.drivingLicenseDataModel.userHasDL) {
                            //stateAutoCompleteTV.setText(it.drivingLicenseDataModel.dlState)
                            drivingLicenseEditText.setText(it.drivingLicenseDataModel.dlNo)
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
        navigate(R.id.addBankDetailsInfoFragment, Bundle().apply {
            putBoolean(AddBankDetailsInfoFragment.CAME_FROM_DRIVING_LICENSE_SCREEN, true)
        })
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