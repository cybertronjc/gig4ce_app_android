package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.documents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.gigforce.app.core.selectItemWithText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details_filled_dialog.UserDetailsFilledDialogFragment
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details_filled_dialog.UserDetailsFilledDialogFragmentResultListener
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.VerificationValidations
import com.gigforce.app.modules.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_ambsd_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_driving_license_info_main.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_driving_license_info_view.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.util.*


enum class DrivingLicenseSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddUserDrivingLicenseInfoFragment : BaseFragment(),
        UserDetailsFilledDialogFragmentResultListener {

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
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null
    private lateinit var userId: String
    private lateinit var userName: String
    private var drivingLicenseDetail: DrivingLicenseDataModel? = null
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_add_driving_license_info, inflater, container)

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
        val adapter =
                ArrayAdapter<String>(
                        requireContext(),
                        R.layout.layout_sp_state_dl,
                        resources.getStringArray(R.array.indian_states)
                )
        stateSpinner.adapter = adapter

        dlFrontImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_driving_license_front_side)
        dlFrontImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_your_driving_license)

        dlBackImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_driving_license_back_side)
        dlBackImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_your_driving_license)
        dlSubmitSliderBtn.isEnabled = false

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }


        helpIconIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()

                if (confirmDLDataCB.isChecked
                        && ((dlSubmitSliderBtn.text == getString(R.string.update)
                                || (dlFrontImagePath != null && dlBackImagePath != null)))
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
                        && ((dlSubmitSliderBtn.text == getString(R.string.update)
                                || (dlFrontImagePath != null && dlBackImagePath != null)))
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

                        if (dlYesRB.isChecked || dlSubmitSliderBtn.text == getString(R.string.update)) {

                            if (stateSpinner.selectedItemPosition == 0) {
                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(getString(R.string.alert))
                                        .setMessage(getString(R.string.select_dl_state))
                                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                        .show()
                                dlSubmitSliderBtn.resetSlider()
                                return
                            }

                            val dlNo =
                                    drivingLicenseEditText.text.toString().toUpperCase(Locale.getDefault())
                            if (!VerificationValidations.isDLNumberValid(dlNo)) {

                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(getString(R.string.alert))
                                        .setMessage(getString(R.string.enter_valid_dl))
                                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                        .show()

                                dlSubmitSliderBtn.resetSlider()
                                return
                            }

                            if (dlSubmitSliderBtn.text != getString(R.string.update) && (dlFrontImagePath == null || dlBackImagePath == null)) {

                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(getString(R.string.alert))
                                        .setMessage(getString(R.string.capture_both_sides_dl))
                                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                        .show()
                                dlSubmitSliderBtn.resetSlider()
                                return
                            }

                            val state = stateSpinner.selectedItem.toString()

                            viewModel.updateDLData(
                                    true,
                                    dlFrontImagePath,
                                    dlBackImagePath,
                                    state,
                                    dlNo,
                                    userId
                            )

                        } else if (dlNoRB.isChecked) {
                            viewModel.updateDLData(
                                    false,
                                    null,
                                    null,
                                    null,
                                    null,
                                    userId
                            )
                        }
                    }
                }


        ambsd_dl_skip_btn.setOnClickListener {

            UserDetailsFilledDialogFragment.launch(
                    userId = userId,
                    userName = userName,
                    fragmentManager = childFragmentManager,
                    okayClickListener = this@AddUserDrivingLicenseInfoFragment
            )
        }

        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.reuploading_driving_license))
                    .setPositiveButton(getString(R.string.okay)) { _, _ ->

                        dlViewLayout.gone()
                        dlMainLayout.visible()

                        setDataOnEditLayout(drivingLicenseDetail)
                        dlSubmitSliderBtn.isEnabled = true
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                    .show()
        }



        dlFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        dlBackImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }

        dlFrontImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.dl_image_front_side)

        dlBackImageHolder.uploadImageLayout.imageLabelTV.text =
                getString(R.string.dl_image_back_side)

        dlFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

        dlBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForBackSideImage()
        }
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                title = getString(R.string.why_do_we_need_this),
                content = getString(R.string.why_we_need_this_dl)
        )
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Are you sure you want to go back")
                .setPositiveButton("Yes") { _, _ -> goBackToUsersList() }
                .setNegativeButton("No") { _, _ -> }
                .show()
    }

    private fun goBackToUsersList() {
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun initViewModel() {

        viewModel.gigerVerificationStatus
                .observe(viewLifecycleOwner, Observer {
                    this.gigerVerificationStatus = it
                    this.drivingLicenseDetail = it.drivingLicenseDataModel
                    progressBar.gone()

                    if (it.dlCardDetailsUploaded && it.drivingLicenseDataModel != null) {
                        if (it.drivingLicenseDataModel.userHasDL != null) {
                            if (it.drivingLicenseDataModel.userHasDL) {
                                setDataOnViewLayout(it)
                            } else {
                                setDataOnEditLayout(null)
                                dlAvailaibilityOptionRG.check(R.id.dlNoRB)
                            }
                        } else {
                            //Uncheck both and hide capture layout
                            setDataOnEditLayout(null)
                            dlAvailaibilityOptionRG.clearCheck()
                            hideDLImageAndInfoLayout()
                        }
                    } else {
                        //Uncheck both and hide capture layout
                        setDataOnEditLayout(null)
                        dlAvailaibilityOptionRG.clearCheck()
                        hideDLImageAndInfoLayout()
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
        dlMainLayout.visibility = View.VISIBLE
        dlSubmitSliderBtn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(error)
                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                .show()
    }

    private fun documentUploaded() {
        showToast(getString(R.string.dl_details_uploaded))

        UserDetailsFilledDialogFragment.launch(
                userId = userId,
                userName = userName,
                fragmentManager = childFragmentManager,
                okayClickListener = this@AddUserDrivingLicenseInfoFragment
        )

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
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.unable_to_capture_image))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
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
                .placeholder(getCircularProgressDrawable())
                .into(dlFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun showBackDrivingLicense(aadharBackImagePath: Uri) {
        dlBackImageHolder.uploadDocumentCardView.visibility = View.GONE
        dlBackImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
                .load(aadharBackImagePath)
                .placeholder(getCircularProgressDrawable())
                .into(dlBackImageHolder.uploadImageLayout.clickedImageIV)
    }

    override fun onOkayClicked() {
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    private fun setDataOnEditLayout(it: DrivingLicenseDataModel?) {
        dlViewLayout.gone()
        dlMainLayout.visible()

        if (it != null) {
            //Fill previous data
            dlAvailaibilityOptionRG.gone()
            doYouHaveDLLabel.gone()
        } else {
            dlAvailaibilityOptionRG.visible()
            doYouHaveDLLabel.visible()

            dlEditOverallErrorMessage.gone()
            dlStateEditErrorMessage.gone()
            dlNoEditErrorMessage.gone()
            dlFrontEditErrorMessage.gone()
            dlBackEditErrorMessage.gone()
        }

        val dlData = it ?: return
        dlSubmitSliderBtn.text = getString(R.string.update)

        drivingLicenseEditText.setText(dlData.dlNo)
        if (dlData.dlState != null) stateSpinner.selectItemWithText(dlData.dlState)

        dlAvailaibilityOptionRG.check(R.id.dlYesRB)


        if (dlData.frontImage != null) {

            if (dlData.frontImage.startsWith("http", true)) {
                showFrontDrivingLicense(Uri.parse(dlData.frontImage))
            } else {

                val imageRef = firebaseStorage
                        .reference
                        .child("verification")
                        .child(dlData.frontImage)

                imageRef.downloadUrl.addOnSuccessListener {
                    showFrontDrivingLicense(it)
                }.addOnFailureListener {
                    print("ee")
                }
            }
        }

        if (dlData.backImage != null) {

            if (dlData.backImage.startsWith("http", true)) {
                showBackDrivingLicense(Uri.parse(dlData.backImage))
            } else {

                val imageRef = firebaseStorage
                        .reference
                        .child("verification")
                        .child(dlData.backImage)

                imageRef.downloadUrl.addOnSuccessListener {
                    showBackDrivingLicense(it)
                }.addOnFailureListener {
                    print("ee")
                }
            }
        }
    }

    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        dlMainLayout.gone()
        dlViewLayout.visible()
        //TODO handle error message when process is ready

        val dlDetails = gigVerificationStatus.drivingLicenseDataModel ?: return

        statusTV.text = dlDetails.verifiedString
        statusTV.setTextColor(
                ResourcesCompat.getColor(
                        resources,
                        gigVerificationStatus.getColorCodeForStatus(dlDetails.state),
                        null
                )
        )

        if (dlDetails.frontImage != null) {

            if (dlDetails.frontImage.startsWith("http", true)) {
                Glide.with(requireContext())
                        .load(dlDetails.frontImage)
                        .placeholder(getCircularProgressDrawable())
                        .into(dlFrontImageIV)
            } else {

                firebaseStorage
                        .reference
                        .child("verification")
                        .child(dlDetails.frontImage)
                        .downloadUrl.addOnSuccessListener {
                            Glide.with(requireContext()).load(it)
                                    .placeholder(getCircularProgressDrawable())
                                    .into(dlFrontImageIV)
                        }.addOnFailureListener {
                            print("ee")
                        }
            }
        }
        dlFrontErrorMessage.gone()

        if (dlDetails.backImage != null) {
            if (dlDetails.backImage.startsWith("http", true)) {
                Glide.with(requireContext())
                        .load(dlDetails.backImage)
                        .placeholder(getCircularProgressDrawable())
                        .into(dlBackImageIV)
            } else {

                firebaseStorage
                        .reference
                        .child("verification")
                        .child(dlDetails.backImage)
                        .downloadUrl.addOnSuccessListener {
                            Glide.with(requireContext()).load(it)
                                    .placeholder(getCircularProgressDrawable())
                                    .into(dlBackImageIV)
                        }.addOnFailureListener {
                            print("ee")
                        }
            }
        }
        dlBackErrorMessage.gone()

        dlNoTV.text = dlDetails.dlNo
        dlNoErrorMessage.gone()

        dlStateTV.text = dlDetails.dlState
        dlStateErrorMessage.gone()
    }

    override fun onReUploadDocumentsClicked() {
        navigate(
                R.id.addUserBankDetailsInfoFragment, bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
        )
        )
    }
}