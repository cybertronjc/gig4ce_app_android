package com.gigforce.app.modules.gigerVerfication.panCard

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
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

class AddPanCardInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    companion object {
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: File? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_pan_card_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }


    private fun initViews() {
        panImageHolder.documentUploadLabelTV.text = getString(R.string.upload_pan_card)
        panImageHolder.documentUploadSubLabelTV.text = getString(R.string.please_upload_your_pan)
        panSubmitSliderBtn.isEnabled = false

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        panImageHolder.uploadDocumentCardView.setOnClickListener {
            launchSelectImageSourceDialog()
        }

        panImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            launchSelectImageSourceDialog()
        }

        panImageHolder.uploadImageLayout.imageLabelTV.text = getString(R.string.pan_card_image)

        panCardAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.panYesRB) {
                showPanImageLayout()

                if (clickedImagePath != null && panDataCorrectCB.isChecked) {
                    showImageInfoLayout()
                    enableSubmitButton()
                } else
                    disableSubmitButton()
            } else if (panDataCorrectCB.isChecked) {
                hidePanImageAndInfoLayout()
                enableSubmitButton()
            } else {
                hidePanImageAndInfoLayout()
            }
        }

        panDataCorrectCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (panYesRB.isChecked && panCardDataModel != null && panCardDataModel?.panCardImagePath != null)
                    enableSubmitButton()
                else if (panNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
        }

        editPanInfoLayout.setOnClickListener {
            navigate(R.id.editPanInfoBottomSheet)
        }

        panSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    if (panYesRB.isChecked)
                        navigate(R.id.addAadharCardInfoFragment)
                    else if (panNoRB.isChecked) {
                        viewModel.updatePanImagePath(false, null)
                        navigate(R.id.addAadharCardInfoFragment)
                    }
                }
            }
    }


    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                updatePanInfo(it)
            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }


    private var panCardDataModel: PanCardDataModel? = null
    private fun updatePanInfo(it: GigerVerificationStatus) {
        if (it.panCardDetailsUploaded && it.panCardDetails != null) {
            this.panCardDataModel = it.panCardDetails
            if (it.panCardDetails.userHasPanCard != null) {
                if (it.panCardDetails.userHasPanCard)
                    panCardAvailaibilityOptionRG.check(R.id.panYesRB)
                else
                    panCardAvailaibilityOptionRG.check(R.id.panNoRB)
            } else {
                //Uncheck both and hide capture layout
                panCardAvailaibilityOptionRG.clearCheck()
                panImageHolder.visibility = View.GONE
            }

            if (it.panCardDetails.panCardImagePath != null) {
                val imageRef = firebaseStorage
                    .reference
                    .child("verification")
                    .child(it.panCardDetails.panCardImagePath)

                imageRef.downloadUrl.addOnSuccessListener {
                    showPanInfoCard(it)
                }.addOnFailureListener {
                    print("ee")
                }
            }
        }
    }


    private fun launchSelectImageSourceDialog() {
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_PURPOSE, PhotoCrop.PURPOSE_UPLOAD_PAN_IMAGE)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "pan_card.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)
    }

    private fun showImageInfoLayout() {
        panInfoLayout.visibility = View.VISIBLE
    }

    private fun showPanImageLayout() {
        panImageHolder.visibility = View.VISIBLE
    }

    private fun hidePanImageAndInfoLayout() {
        panImageHolder.visibility = View.GONE
        panInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        panSubmitSliderBtn.isEnabled = true

        panSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        panSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    private fun disableSubmitButton() {
        panSubmitSliderBtn.isEnabled = false

        panSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        panSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        clickedImagePath = File("na")

        if (panDataCorrectCB.isChecked)
            enableSubmitButton()

//        showPanInfoCard(clickedImagePath)
        setPanInfoOnView(
            name = "Rahul Jain",
            fathersName = "Sahil Jain",
            dob = "11/09/1990",
            pan = "PU23SDDLOJIJ"
        )
    }

    private fun showPanInfoCard(panInfoPath: Uri) {
        panImageHolder.uploadDocumentCardView.visibility = View.GONE
        panImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(panInfoPath)
            .into(panImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setPanInfoOnView(
        name: String?,
        fathersName: String?,
        dob: String?,
        pan: String?
    ) {
        nameTV.text = name
        fathersNameTV.text = fathersName
        dobTV.text = dob
        panNoTV.text = pan
    }

}