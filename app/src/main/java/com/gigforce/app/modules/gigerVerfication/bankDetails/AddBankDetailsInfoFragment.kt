package com.gigforce.app.modules.gigerVerfication.bankDetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

class AddBankDetailsInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    companion object {
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: File? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

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
            activity?.onBackPressed()
        }

        passbookSubmitSliderBtn.isEnabled = false
        passbookImageHolder.uploadDocumentCardView.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            showCameraAndGalleryOption()
        }

        passbookImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_bank_passbook)

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->
            passbookSubmitSliderBtn.resetSlider()

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageLayout()

                if (bankDetailsDataConfirmationCB.isChecked) {
                    //showPassbookInfoLayout()
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

        editBankDetailsLayout.setOnClickListener {
            navigate(R.id.editBankDetailsInfoBottomSheet)
        }

        passbookSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {

                    if (passbookNoRB.isChecked) {
                        viewModel.updateBankPassbookImagePath(
                            userHasPassBook = false,
                            passbookImagePath = null
                        )

                        findNavController().popBackStack(R.id.gigerVerificationFragment, true)
                        activity?.onBackPressed()
                    } else {
                        findNavController().popBackStack(R.id.gigerVerificationFragment, true)
                        activity?.onBackPressed()
                    }
                }
            }
    }

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if (it.bankDetailsUploaded && it.bankUploadDetailsDataModel != null) {

                    if (it.bankUploadDetailsDataModel.userHasPassBook != null) {
                        if (it.bankUploadDetailsDataModel.userHasPassBook)
                            passbookAvailaibilityOptionRG.check(R.id.passbookYesRB)
                        else
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

                        clickedImagePath = File(it.bankUploadDetailsDataModel.passbookImagePath)
                        if (bankDetailsDataConfirmationCB.isChecked)
                            passbookSubmitSliderBtn.isEnabled = true


                        imageRef.downloadUrl.addOnSuccessListener {
                            showPanInfoCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
                    }
                }
            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }


    private fun showCameraAndGalleryOption() {
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_UPLOAD_BANK_DETAILS_IMAGE
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "pan_card.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)
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

    override fun onImageSourceSelected(source: ImageSource) {
//        showPassbookInfoLayout()
//        bankDetailsDataConfirmationCB.visibility = View.VISIBLE
//
//        clickedImagePath = File("na")
//        showPanInfoCard()
//        setBankDetailsInfoOnView(
//            name = "Rahul Jain",
//            fathersName = "Sahil Jain",
//            cifNumber = "TF-334",
//            accountNumber = "PU23SDDLOJIJ",
//            ifsc = "PKSSM09233",
//            address = "House no 342, Preet Vihar,New Delhi, Delhi 113320"
//        )
    }

    private fun showPanInfoCard(panInfoPath: Uri) {
        passbookImageHolder.uploadDocumentCardView.visibility = View.GONE
        passbookImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(panInfoPath)
            .into(passbookImageHolder.uploadImageLayout.clickedImageIV)
    }


    private fun setBankDetailsInfoOnView(
        name: String?,
        fathersName: String?,
        cifNumber: String?,
        accountNumber: String?,
        ifsc: String?,
        address: String?
    ) {
        nameTV.text = name
        fathersNameTV.text = fathersName
        cifNumberTV.text = cifNumber
        accNoTV.text = accountNumber
        ifscTV.text = ifsc
        addressTV.text = address
    }

}