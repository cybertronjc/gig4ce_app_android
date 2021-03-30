package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.documents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
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
import com.gigforce.app.modules.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.app.modules.gigerVerfication.aadharCard.AadharCardDataModel
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_ambsd_add_aadhar_card_info.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_aadhar_card_info_main.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_aadhar_card_view.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

enum class AadharCardSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddUserAadharCardInfoFragment : BaseFragment() {

    companion object {
        const val REQUEST_CODE_UPLOAD_AADHAR_IMAGE = 2333
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String

    private var aadharFrontImagePath: Uri? = null
    private var aadharBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var aadharCardDataModel: AadharCardDataModel? = null

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_add_aadhar_card_info, inflater, container)

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
        aadharFrontImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_aadhar_card_front_side)
        aadharFrontImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_your_aadhar_card)

        aadharBackImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_aadhar_card_back_side)
        aadharBackImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_your_aadhar_card)

        aadharSubmitSliderBtn.isEnabled = false

        ambsd_aadhar_aahdar_skip_btn.setOnClickListener {

            navigate(
                    R.id.addUserDrivingLicenseInfoFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            )

        }

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }

        helpIconViewIV.setOnClickListener {
            showWhyWeNeedThisBottomSheet()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisBottomSheet()
        }

        aadharAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.aadharYesRB) {
                showAadharImageAndInfoLayout()
                showImageInfoLayout()
                enableSubmitButton()

            } else if (checkedId == R.id.aadharNoRB) {
                hideAadharImageAndInfoLayout()

                aadharSubmitSliderBtn.visible()
                enableSubmitButton()
            } else {
                hideAadharImageAndInfoLayout()
                disableSubmitButton()
            }
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

        ambsd_aadhar_skip_btn.setOnClickListener {

            navigate(R.id.addUserDrivingLicenseInfoFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            )
        }

        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.you_are_reuploading_aadhar))
                    .setPositiveButton(getString(R.string.okay)) { _, _ ->

                        aadharViewLayout.gone()
                        aadharEditLayout.visible()

                        setDataOnEditLayout(aadharCardDataModel)
                        aadharAvailaibilityOptionRG.check(R.id.aadharYesRB)
                        aadharSubmitSliderBtn.isEnabled = true
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                    .show()
        }



        aadharSubmitSliderBtn.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {

                    override fun onSlideComplete(view: SlideToActView) {

                        if (aadharYesRB.isChecked || aadharSubmitSliderBtn.text == getString(R.string.update)) {
                            if (aadharCardET.text!!.length != 12) {

                                aadharEditLayout.post {
                                    aadharEditLayout.scrollTo(0, topSeaparator.y.toInt())
                                }

                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(getString(R.string.alert))
                                        .setMessage(getString(R.string.enter_valid_aadhar_no))
                                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                        .show()
                                aadharSubmitSliderBtn.resetSlider()
                                return
                            }


                            if (aadharSubmitSliderBtn.text != getString(R.string.update) && (aadharFrontImagePath == null || aadharBackImagePath == null)) {

                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(getString(R.string.alert))
                                        .setMessage(getString(R.string.select_or_capture_both_sides_of_aadhar))
                                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                        .show()
                                aadharSubmitSliderBtn.resetSlider()
                                return
                            }

                            val aadharNo = aadharCardET.text.toString()

                            viewModel.updateAadharData(
                                    true,
                                    aadharFrontImagePath,
                                    aadharBackImagePath,
                                    aadharNo,
                                    userId
                            )

                        } else if (aadharNoRB.isChecked) {
                            viewModel.updateAadharData(false, null, null, null, userId)
                        }
                    }
                }


    }

    private fun showWhyWeNeedThisBottomSheet() {
        WhyWeNeedThisBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                title = getString(R.string.why_do_we_need_this),
                content = getString(R.string.why_we_need_this_aadhar)
        )
    }

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
                .observe(viewLifecycleOwner, Observer {
                    this.gigerVerificationStatus = it
                    this.aadharCardDataModel = it.aadharCardDataModel
                    progressBar.gone()

                    if (it.aadharCardDetailsUploaded && it.aadharCardDataModel != null) {

                        if (it.aadharCardDataModel.userHasAadharCard != null) {
                            if (it.aadharCardDataModel.userHasAadharCard) {
                                setDataOnViewLayout(it)
                            } else {
                                setDataOnEditLayout(null)
                                aadharAvailaibilityOptionRG.check(R.id.aadharNoRB)
                            }

                        } else {
                            //Uncheck both and hide capture layout
                            setDataOnEditLayout(null)
                            aadharAvailaibilityOptionRG.clearCheck()
                            hideAadharImageAndInfoLayout()
                        }
                    } else {
                        setDataOnEditLayout(null)
                        aadharAvailaibilityOptionRG.clearCheck()
                        hideAadharImageAndInfoLayout()
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
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        aadharViewLayout.gone()
        aadharEditLayout.visibility = View.VISIBLE
        aadharSubmitSliderBtn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(error)
                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                .show()
    }

    private fun documentUploaded() {
        showToast(getString(R.string.aadhar_card_details_uploaded))

        navigate(
                R.id.addUserDrivingLicenseInfoFragment, bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
        )
        )
    }

    private fun showLoadingState() {
        aadharViewLayout.gone()
        aadharEditLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
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
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
                PhotoCrop.INTENT_EXTRA_PURPOSE,
                PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
                PhotoCrop.INTENT_EXTRA_PURPOSE,
                PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_back.jpg")
        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPLOAD_AADHAR_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {

                if (AadharCardSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    aadharFrontImagePath =
                            data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                    showFrontAadharCard(aadharFrontImagePath!!)
                } else if (AadharCardSides.BACK_SIDE == currentlyClickingImageOfSide) {
                    aadharBackImagePath =
                            data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                    showBackAadharCard(aadharBackImagePath!!)
                }

                    enableSubmitButton()

                if (aadharFrontImagePath != null && aadharBackImagePath != null && aadharSubmitSliderBtn.isGone) {
                    aadharSubmitSliderBtn.visible()
                }

            } else {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.unable_to_capture_image))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()
            }
        }
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


    private fun showFrontAadharCard(aadharFrontImagePath: Uri) {
        aadharFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
        aadharFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE
        aadharFrontImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.aadhar_card_front_image)

        Glide.with(requireContext())
                .load(aadharFrontImagePath)
                .placeholder(getCircularProgressDrawable())
                .into(aadharFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun showBackAadharCard(aadharBackImagePath: Uri) {
        aadharBackImageHolder.uploadDocumentCardView.visibility = View.GONE
        aadharBackImageHolder.uploadImageLayout.visibility = View.VISIBLE
        aadharBackImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.aadhar_card_back_image)

        Glide.with(requireContext())
                .load(aadharBackImagePath)
                .placeholder(getCircularProgressDrawable())
                .into(aadharBackImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        aadharEditLayout.gone()
        aadharViewLayout.visible()

        val aadharDetails = gigVerificationStatus.aadharCardDataModel ?: return

        statusTV.text = aadharDetails.verifiedString
        statusTV.setTextColor(
                ResourcesCompat.getColor(
                        resources,
                        gigVerificationStatus.getColorCodeForStatus(aadharDetails.state),
                        null
                )
        )

        if (aadharDetails.frontImage != null) {
            if (aadharDetails.frontImage.startsWith("http", true)) {
                Glide.with(requireContext()).load(aadharDetails.frontImage)
                        .placeholder(getCircularProgressDrawable()).into(aadharViewFrontImageIV)
            } else {
                val storageRef = firebaseStorage
                        .reference
                        .child("verification")
                        .child(aadharDetails.frontImage)

                Glide.with(requireContext())
                        .load(storageRef)
                        .placeholder(getCircularProgressDrawable())
                        .into(aadharViewFrontImageIV)
            }
        }
        aadharViewFrontErrorMessage.gone()

        if (aadharDetails.backImage != null) {
            if (aadharDetails.backImage.startsWith("http", true)) {
                Glide.with(requireContext()).load(aadharDetails.backImage)
                        .placeholder(getCircularProgressDrawable()).into(aadharViewBackImageIV)
            } else {
                val imageRef = firebaseStorage
                        .reference
                        .child("verification")
                        .child(aadharDetails.backImage)

                Glide
                        .with(requireContext())
                        .load(imageRef)
                        .placeholder(getCircularProgressDrawable())
                        .into(aadharViewBackImageIV)
            }
        }
        aadharViewBackErrorMessageTV.gone()

        aadharNoTV.text = aadharDetails.aadharCardNo
        aadharNumberViewErrorMessage.gone()
    }

    private fun setDataOnEditLayout(it: AadharCardDataModel?) {
        aadharViewLayout.gone()
        aadharEditLayout.visible()

        if (it != null) {
            //Fill previous data
            aadharAvailaibilityOptionRG.gone()
            doYouHaveAadharLabel.gone()
        } else {
            aadharAvailaibilityOptionRG.visible()
            doYouHaveAadharLabel.visible()

            aadharEditOverallErrorMessage.gone()
            aadharNoEditErrorMessage.gone()
            aadharFrontImageEditErrorMessage.gone()
            aadharBackImageEditErrorMessage.gone()
        }

        val aadharData = it ?: return
        aadharSubmitSliderBtn.text = getString(R.string.update)

        aadharCardET.setText(aadharData.aadharCardNo)


        if (aadharData.frontImage != null) {
            if (aadharData.frontImage.startsWith("http", true)) {
                showFrontAadharCard(Uri.parse(aadharData.frontImage))
            } else {
                firebaseStorage
                        .reference
                        .child("verification")
                        .child(aadharData.frontImage)
                        .downloadUrl.addOnSuccessListener {
                            showFrontAadharCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
            }
        }

        if (aadharData.backImage != null) {
            if (aadharData.backImage.startsWith("http", true)) {
                showBackAadharCard(Uri.parse(aadharData.backImage))
            } else {
                firebaseStorage
                        .reference
                        .child("verification")
                        .child(aadharData.backImage)
                        .downloadUrl.addOnSuccessListener {
                            showBackAadharCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
            }
        }
    }

}