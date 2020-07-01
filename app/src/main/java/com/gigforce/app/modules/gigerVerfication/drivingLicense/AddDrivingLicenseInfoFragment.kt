package com.gigforce.app.modules.gigerVerfication.drivingLicense

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import com.gigforce.app.modules.gigerVerfication.aadharCard.AddAadharCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

enum class DrivingLicenseSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddDrivingLicenseInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    private val viewModel: GigVerificationViewModel by viewModels()

    private var dlFrontImagePath: File? = null
    private var dlBackImagePath: File? = null
    private var drivingLicenseDetail: DrivingLicenseDataModel? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null

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
            activity?.onBackPressed()
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()

                if (confirmDLDataCB.isChecked
                        && drivingLicenseDetail != null
                        && drivingLicenseDetail?.frontImage != null
                        && drivingLicenseDetail?.backImage != null) {
                    enableSubmitButton()
                }

            } else if (checkedId == R.id.dlNoRB && confirmDLDataCB.isChecked) {
                hideDLImageAndInfoLayout()
                enableSubmitButton()
            } else
                disableSubmitButton()
        }

        confirmDLDataCB.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {

                if (dlYesRB.isChecked
                        && drivingLicenseDetail != null
                        && drivingLicenseDetail?.frontImage != null
                        && drivingLicenseDetail?.backImage != null)
                    enableSubmitButton()
                else if (dlNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
        }

        dlSubmitSliderBtn.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {

            override fun onSlideComplete(view: SlideToActView) {

                if (dlYesRB.isChecked)
                    navigate(R.id.addBankDetailsInfoFragment)
                else if (dlNoRB.isChecked) {
                    viewModel.updateDLData(false, null, null)
                    navigate(R.id.addBankDetailsInfoFragment)
                }
            }
        }

        editDrivingLicenseInfoLayout.setOnClickListener {
            navigate(R.id.editDrivingLicenseInfoBottomSheet)
        }

        dlFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        dlBackImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }

        dlFrontImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.upload_driving_license_front_side)

        dlBackImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.upload_driving_license_back_side)

        dlFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        dlBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }
    }

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
                .observe(viewLifecycleOwner, Observer {

                    if (it.dlCardDetailsUploaded && it.drivingLicenseDataModel != null) {
                        this.drivingLicenseDetail = it.drivingLicenseDataModel

                        if (it.drivingLicenseDataModel.userHasDL != null) {
                            if (it.drivingLicenseDataModel.userHasDL)
                                dlAvailaibilityOptionRG.check(R.id.dlYesRB)
                            else
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

        viewModel.startListeningForGigerVerificationStatusChanges()
    }


    private fun openCameraAndGalleryOptionForFrontSideImage() {
//        currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE
//
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
                PhotoCrop.INTENT_EXTRA_PURPOSE,
                PhotoCrop.PURPOSE_UPLOAD_DL_FRONT_IMAGE
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
        startActivityForResult(
                photoCropIntent,
                AddAadharCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE
        )

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
//        currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE
//
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
                PhotoCrop.INTENT_EXTRA_PURPOSE,
                PhotoCrop.PURPOSE_UPLOAD_DL_BACK_IMAGE
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_back.jpg")
        startActivityForResult(
                photoCropIntent,
                AddAadharCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE
        )
    }

    private fun showDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.VISIBLE
        dlFrontImageHolder.visibility = View.VISIBLE
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

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        if (currentlyClickingImageOfSide == null)
            return
        else if (currentlyClickingImageOfSide == DrivingLicenseSides.BACK_SIDE) {
            dlBackImagePath = File("ma")

            if (confirmDLDataCB.isChecked)
                enableSubmitButton()

//            showBackDrivingLicense()
        } else if (currentlyClickingImageOfSide == DrivingLicenseSides.FRONT_SIDE) {
            dlFrontImagePath = File("ma")

            if (confirmDLDataCB.isChecked)
                enableSubmitButton()

            //showFrontDrivingLicense()
        }

        setDLInfoOnView(
                name = "Rahul Jain",
                dob = "11/09/1990",
                fathersName = "Male",
                licenseNo = "DL234576249238",
                licenseValidity = "10/2030",
                address = "House no 3432, Preet Vihar, New Delhi, Delhi 112034"
        )
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


    private fun setDLInfoOnView(
            name: String?,
            fathersName: String?,
            dob: String?,
            licenseNo: String?,
            licenseValidity: String?,
            address: String?
    ) {
        nameTV.text = name
        fathersNameTV.text = fathersName
        dobTV.text = dob
        licenseNoTV.text = licenseNo
        licenseValidityTV.text = licenseValidity
        addOnlicenseTV.text = address
    }

}