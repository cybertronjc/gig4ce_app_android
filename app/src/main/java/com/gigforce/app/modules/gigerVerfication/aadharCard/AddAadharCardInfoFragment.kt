package com.gigforce.app.modules.gigerVerfication.aadharCard

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
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

enum class AadharCardSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddAadharCardInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    companion object {
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333
    }

    private val viewModel: GigVerificationViewModel by viewModels()

    private var aadharCardDataModel: AadharCardDataModel? = null
    private var aadharFrontImagePath: File? = null
    private var aadharBackImagePath: File? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_aadhar_card_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViews() {
        aadharFrontImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_aadhar_card_front_side)
        aadharFrontImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_aadhar_card)

        aadharBackImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_aadhar_card_back_side)
        aadharBackImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_aadhar_card)

        aadharSubmitSliderBtn.isEnabled = false

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        aadharAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.aadharYesRB) {
                showAadharImageAndInfoLayout()
            } else if (checkedId == R.id.aadharNoRB && aadharDataCorrectCB.isChecked) {
                hideAadharImageAndInfoLayout()
                enableSubmitButton()
            } else
                disableSubmitButton()
        }

        aadharDataCorrectCB.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {

                if (aadharYesRB.isChecked && aadharCardDataModel != null && aadharCardDataModel?.frontImage != null && aadharCardDataModel?.backImage != null)
                    enableSubmitButton()
                else if (aadharNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
        }

        aadharFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        aadharFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        aadharBackImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }

        aadharBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }

        aadharEditLayout.setOnClickListener {
            navigate(R.id.editAadharInfoBottomSheet)
        }

        aadharSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    if (aadharYesRB.isChecked)
                        navigate(R.id.addDrivingLicenseInfoFragment)
                    else if (aadharNoRB.isChecked) {
                        viewModel.updateAadharData(false, null, null)
                        navigate(R.id.addDrivingLicenseInfoFragment)
                    }
                }
            }
    }

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if (it.aadharCardDetailsUploaded && it.aadharCardDataModel != null) {
                    this.aadharCardDataModel = it.aadharCardDataModel

                    if (it.aadharCardDataModel.userHasAadharCard != null) {
                        if (it.aadharCardDataModel.userHasAadharCard)
                            aadharAvailaibilityOptionRG.check(R.id.aadharYesRB)
                        else
                            aadharAvailaibilityOptionRG.check(R.id.aadharNoRB)
                    } else {
                        //Uncheck both and hide capture layout
                        aadharAvailaibilityOptionRG.clearCheck()
                        hideAadharImageAndInfoLayout()
                    }

                    if (it.aadharCardDataModel.frontImage != null) {
                        val imageRef = firebaseStorage
                            .reference
                            .child("verification")
                            .child(it.aadharCardDataModel.frontImage)

                        imageRef.downloadUrl.addOnSuccessListener {
                            showFrontAadharCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
                    }

                    if (it.aadharCardDataModel.backImage != null) {
                        val imageRef = firebaseStorage
                            .reference
                            .child("verification")
                            .child(it.aadharCardDataModel.backImage)

                        imageRef.downloadUrl.addOnSuccessListener {
                            showBackAadharCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
                    }

                    if (aadharDataCorrectCB.isChecked && it.aadharCardDataModel.frontImage != null && it.aadharCardDataModel.backImage != null) {
                        enableSubmitButton()
                    }
                }

            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }


    private fun openCameraAndGalleryOptionForFrontSideImage() {
//        currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE
//
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_UPLOAD_AADHAR_FRONT_IMAGE
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
//        currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE
//
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_UPLOAD_AADHAR_BACK_IMAGE
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_back.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)
    }

    private fun showAadharImageAndInfoLayout() {
        aadharBackImageHolder.visibility = View.VISIBLE
        aadharFrontImageHolder.visibility = View.VISIBLE
    }

    private fun hideAadharImageAndInfoLayout() {
        aadharBackImageHolder.visibility = View.GONE
        aadharFrontImageHolder.visibility = View.GONE
        aadharInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        aadharSubmitSliderBtn.isEnabled = true

        aadharSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        aadharSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    private fun disableSubmitButton() {
        aadharSubmitSliderBtn.isEnabled = false

        aadharSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        aadharSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
    }

    private fun showImageInfoLayout() {
        aadharInfoLayout.visibility = View.VISIBLE
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        if (currentlyClickingImageOfSide == null)
            return
        else if (currentlyClickingImageOfSide == AadharCardSides.BACK_SIDE) {
            aadharBackImagePath = File("ma")
            //   showBackAadharCard(aadharBackImagePath)

            if (aadharDataCorrectCB.isChecked)
                enableSubmitButton()

        } else if (currentlyClickingImageOfSide == AadharCardSides.FRONT_SIDE) {
            aadharFrontImagePath = File("ma")
            // showFrontAadharCard(aadharFrontImagePath)

            if (aadharDataCorrectCB.isChecked)
                enableSubmitButton()
        }

        setAadharInfoOnView(
            name = "Rahul Jain",
            dob = "11/09/1990",
            gender = "Male",
            aadharNo = "2345 7624 9238",
            address = "House no 3601, PT-Vihar, New Delhi, Delhi 110033"
        )
    }

    private fun showFrontAadharCard(aadharFrontImagePath: Uri) {
        aadharFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
        aadharFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE
        aadharFrontImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_aadhar_card_front_side)

        Glide.with(requireContext())
            .load(aadharFrontImagePath)
            .into(aadharFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun showBackAadharCard(aadharBackImagePath: Uri) {
        aadharBackImageHolder.uploadDocumentCardView.visibility = View.GONE
        aadharBackImageHolder.uploadImageLayout.visibility = View.VISIBLE
        aadharBackImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_aadhar_card_back_side)

        Glide.with(requireContext())
            .load(aadharBackImagePath)
            .into(aadharBackImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setAadharInfoOnView(
        name: String?,
        dob: String?,
        gender: String?,
        aadharNo: String?,
        address: String?
    ) {
        nameTV.text = name
        dobTV.text = dob
        genderTV.text = gender
        aadharNoTV.text = aadharNo
        addressTV.text = address
    }

}