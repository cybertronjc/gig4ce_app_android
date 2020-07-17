package com.gigforce.app.modules.gigerVerfication.panCard

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
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import com.gigforce.app.modules.gigerVerfication.aadharCard.AddAadharCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.*
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_main.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

class AddPanCardInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    companion object {
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path"
        const val INTENT_EXTRA_PAN = "pan"
        const val CAME_FROM_SELFIE_SCREEN = "came_from_selfie"
    }


    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var cameFromSelfieScreen: Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_pan_card_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()

        arguments?.let {
            cameFromSelfieScreen = it.getBoolean(CAME_FROM_SELFIE_SCREEN)
        }

        savedInstanceState?.let {
            cameFromSelfieScreen = it.getBoolean(CAME_FROM_SELFIE_SCREEN)

//            clickedImagePath = it.getParcelable(INTENT_EXTRA_CLICKED_IMAGE_PATH)
//            if (clickedImagePath != null) showPanInfoCard(clickedImagePath!!)
//            panCardEditText.setText(it.getString(INTENT_EXTRA_PAN))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CAME_FROM_SELFIE_SCREEN, cameFromSelfieScreen)
    }

    private fun initViews() {
        panImageHolder.documentUploadLabelTV.text = getString(R.string.upload_pan_card)
        panImageHolder.documentUploadSubLabelTV.text = getString(R.string.please_upload_your_pan)
        panSubmitSliderBtn.isEnabled = false

        toolbar.setNavigationOnClickListener {

            if (cameFromSelfieScreen)
                findNavController().popBackStack(R.id.addSelfieVideoFragment, false)
            else
                activity?.onBackPressed()
        }

        panImageHolder.uploadDocumentCardView.setOnClickListener {
            launchSelectImageSourceDialog()
        }

        panImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            launchSelectImageSourceDialog()
        }

        panImageHolder.uploadImageLayout.imageLabelTV.text = getString(R.string.pan_card_image)

        panCardEditText.doOnTextChanged { text, start, count, after ->
            panCardNoTextInputLayout.error = null
        }

        panCardAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.panYesRB) {
                showPanImageLayout()
                showImageInfoLayout()

                if (clickedImagePath != null && panDataCorrectCB.isChecked) {
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

                if (panYesRB.isChecked && clickedImagePath != null)
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

                        if (panYesRB.isChecked) {
                            if (panCardEditText.text!!.length != 10) {
                                panCardNoTextInputLayout.error = "Enter Valid PAN Card No"
                                return
                            }

                            if (clickedImagePath == null) {

                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Alert")
                                        .setMessage("Click Or Select your Pan card Image first")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .show()
                                return
                            }

                            val panNo = panCardEditText.text.toString()
                            viewModel.updatePanImagePath(true, clickedImagePath, panNo)
                        } else if (panNoRB.isChecked) {
                            viewModel.updatePanImagePath(false, null, null)

                        }
                    }
                }
    }


    private fun initViewModel() {
        viewModel.gigerVerificationStatus
                .observe(viewLifecycleOwner, Observer {
                    updatePanInfo(it)
                })

        viewModel.documentUploadState
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        Lse.Loading -> showLoadingState()
                        Lse.Success -> panCardDocumentUploaded()
                        is Lse.Error -> errorOnUploadingDocuments(it.error)
                    }
                })

        viewModel.getVerificationStatus()
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        panCardMainLayout.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage(error)
                .setPositiveButton("OK") { _, _ -> }
                .show()
    }

    private fun panCardDocumentUploaded() {
        showToast("Pan Card Details Uploaded")
        navigate(R.id.addAadharCardInfoFragment,Bundle().apply {
            putBoolean(AddAadharCardInfoFragment.CAME_FROM_PAN_SCREEN,true)
        })
    }

    private fun showLoadingState() {
        panCardMainLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onBackPressed(): Boolean {

        if (cameFromSelfieScreen) {
            findNavController().popBackStack(R.id.addSelfieVideoFragment, true)
            return true
        } else {
           return super.onBackPressed()
        }
    }

    private var panCardDataModel: PanCardDataModel? = null
    private fun updatePanInfo(it: GigerVerificationStatus) {
        if (it.panCardDetailsUploaded && it.panCardDetails != null) {
            this.panCardDataModel = it.panCardDetails
            if (it.panCardDetails.userHasPanCard != null) {
                if (it.panCardDetails.userHasPanCard) {
                    panCardAvailaibilityOptionRG.check(R.id.panYesRB)
                    panCardEditText.setText(it.panCardDetails.panCardNo)
                } else
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
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_PURPOSE, PhotoCrop.PURPOSE_VERIFICATION)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "pan_card.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPLOAD_PAN_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {
                clickedImagePath =
                        data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                showPanInfoCard(clickedImagePath!!)
            } else {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Unable to Capture Image")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
            }
        }
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

        if (panDataCorrectCB.isChecked)
            enableSubmitButton()
    }

    private fun showPanInfoCard(panInfoPath: Uri) {
        panImageHolder.uploadDocumentCardView.visibility = View.GONE
        panImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
                .load(panInfoPath)
                .into(panImageHolder.uploadImageLayout.clickedImageIV)
    }


}