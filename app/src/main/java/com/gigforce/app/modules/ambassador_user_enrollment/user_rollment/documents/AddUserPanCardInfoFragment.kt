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
import com.gigforce.app.modules.gigerVerfication.*
import com.gigforce.core.datamodels.verification.PanCardDataModel
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info.panEditLayout
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info.panViewLayout
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info.progressBar
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.doYouHavePanCardLabel
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.helpIconIV
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panCardAvailaibilityOptionRG
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panCardEditText
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panDataCorrectCB
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panEditOverallErrorMessage
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panImageEditErrorMessage
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panImageHolder
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panInfoLayout
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panNoEditErrorMessage
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panNoRB
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panSubmitSliderBtn
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.panYesRB
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_main.whyWeNeedThisTV
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.editLayout
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.panViewImageErrorMessage
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.panViewImageIV
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.panViewNoErrorMessage
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.panViewNoTV
import kotlinx.android.synthetic.main.fragment_ambsd_add_pan_card_info_view.statusTV
import java.util.*

class AddUserPanCardInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    companion object {
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path"
        const val INTENT_EXTRA_PAN = "pan"
    }

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private lateinit var userId : String
    private lateinit var userName : String
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var panCardDataModel: PanCardDataModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_add_pan_card_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments,savedInstanceState)
        initViews()
        initViewModel()
        getExistingDocumentsDetails()
    }

    private fun getExistingDocumentsDetails() {
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
        panImageHolder.documentUploadLabelTV.text = getString(R.string.upload_pan_card)
        panImageHolder.documentUploadSubLabelTV.text = getString(R.string.please_upload_your_pan)
        panSubmitSliderBtn.isEnabled = false

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }

        helpIconIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisDialog()
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
                showImageInfoLayout()

                if (panDataCorrectCB.isChecked && (panSubmitSliderBtn.text == getString(R.string.update)
                            || clickedImagePath != null)) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()
            } else if (checkedId == R.id.panNoRB) {
                hidePanImageAndInfoLayout()

                panDataCorrectCB.visible()
                panSubmitSliderBtn.visible()

                if (panDataCorrectCB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else {
                hidePanImageAndInfoLayout()
            }
        }

        panDataCorrectCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (panYesRB.isChecked && (panSubmitSliderBtn.text == getString(R.string.update) || clickedImagePath != null))
                    enableSubmitButton()
                else if (panNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
        }

        ambsd_pan_skip_btn.setOnClickListener {

            navigate(
                    R.id.addUserAadharCardInfoFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            )
        }

        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.you_are_reuploading_pan_card))
                    .setPositiveButton(getString(R.string.okay)) { _, _ ->

                        panViewLayout.gone()
                        panEditLayout.visible()

                        setDataOnEditLayout(panCardDataModel)
                        panCardAvailaibilityOptionRG.check(R.id.panYesRB)
                        panSubmitSliderBtn.isEnabled = true
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                    .show()
        }



        panSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    if (panYesRB.isChecked || panSubmitSliderBtn.text == getString(R.string.update)) {
                        val panCardNo =
                            panCardEditText.text.toString().toUpperCase(Locale.getDefault())
                        if (!VerificationValidations.isPanCardValid(panCardNo)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.enter_valid_pan))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()

                            panSubmitSliderBtn.resetSlider()
                            return
                        }

                        if (panSubmitSliderBtn.text != getString(R.string.update) && clickedImagePath == null) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.click_select_pan_image))
                                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                                .show()
                            panSubmitSliderBtn.resetSlider()
                            return
                        }

                        viewModel.updatePanImagePath(true, clickedImagePath, panCardNo, userId)
                    } else if (panNoRB.isChecked) {
                        viewModel.updatePanImagePath(false, null, null, userId)
                    }
                }
            }
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this),
            content = getString(R.string.why_do_we_need_this_pan)
        )
    }


    private fun initViewModel() {

        viewModel.gigerVerificationStatus
                .observe(viewLifecycleOwner, Observer {
                    this.gigerVerificationStatus = it
                    this.panCardDataModel = it.panCardDetails
                    progressBar.gone()

                    if (it.panCardDetailsUploaded && it.panCardDetails != null) {

                        if (it.panCardDetails.userHasPanCard != null) {
                            if (it.panCardDetails.userHasPanCard!!) {
                                setDataOnViewLayout(it)
                            } else {
                                setDataOnEditLayout(null)
                                panCardAvailaibilityOptionRG.check(R.id.panNoRB)
                            }
                        } else {
                            //Uncheck both and hide capture layout
                            setDataOnEditLayout(null)
                            panCardAvailaibilityOptionRG.clearCheck()
                            hidePanImageAndInfoLayout()
                        }
                    } else {
                        setDataOnEditLayout(null)
                        panCardAvailaibilityOptionRG.clearCheck()
                        hidePanImageAndInfoLayout()
                    }

                })

        viewModel.documentUploadState
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lse.Loading -> showLoadingState()
                    Lse.Success -> panCardDocumentUploaded()
                    is Lse.Error -> errorOnUploadingDocuments(it.error)
                }
            })
    }



    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        panViewLayout.gone()
        panEditLayout.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
            .show()
    }

    private fun panCardDocumentUploaded() {
        showToast(getString(R.string.pan_details_uploaded))

        navigate(
            R.id.addUserAadharCardInfoFragment, bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
        )
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert))
            .setMessage(getString(R.string.are_u_sure_u_want_to_go_back))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> goBackToUsersList() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun goBackToUsersList(){
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun showLoadingState() {
        panEditLayout.visibility = View.GONE
        panViewLayout.gone()
        progressBar.visibility = View.VISIBLE
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
                clickedImagePath = data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
                showPanInfoCard(clickedImagePath!!)

                if (panDataCorrectCB.isChecked)
                    enableSubmitButton()

                if (clickedImagePath != null && panSubmitSliderBtn.isGone) {
                    panSubmitSliderBtn.visible()
                    panDataCorrectCB.visible()
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
            .placeholder(getCircularProgressDrawable())
            .into(panImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        panEditLayout.gone()
        panViewLayout.visible()

        val panDetails = gigVerificationStatus.panCardDetails ?: return

        statusTV.text = panDetails.verifiedString
        statusTV.setTextColor(
                ResourcesCompat.getColor(
                        resources,
                        gigVerificationStatus.getColorCodeForStatus(panDetails.state),
                        null
                )
        )

        if (panDetails.panCardImagePath != null) {

            if (panDetails.panCardImagePath!!.startsWith("http", true)) {
                Glide.with(requireContext()).load(panDetails.panCardImagePath).placeholder(getCircularProgressDrawable()).into(panViewImageIV)
            } else {
                val storageRef = firebaseStorage
                        .reference
                        .child("verification")
                        .child(panDetails.panCardImagePath!!)

                Glide.with(requireContext())
                        .load(storageRef)
                        .placeholder(getCircularProgressDrawable())
                        .into(panViewImageIV)

            }
        }
        panViewImageErrorMessage.gone()

        panViewNoTV.text = panDetails.panCardNo
        panViewNoErrorMessage.gone()
    }

    private fun setDataOnEditLayout(it: PanCardDataModel?) {
        panViewLayout.gone()
        panEditLayout.visible()

        if (it != null) {
            //Fill previous data
            panCardAvailaibilityOptionRG.gone()
            doYouHavePanCardLabel.gone()
        } else {
            panCardAvailaibilityOptionRG.visible()
            doYouHavePanCardLabel.visible()

            panEditOverallErrorMessage.gone()
            panNoEditErrorMessage.gone()
            panImageEditErrorMessage.gone()
        }

        panInfoLayout.visible()
        panImageHolder.visible()

        val panData = it ?: return
        panSubmitSliderBtn.text = getString(R.string.update)

        panCardEditText.setText(panData.panCardNo)

        if (panData.panCardImagePath != null) {
            if (panData.panCardImagePath!!.startsWith("http", true)) {
                showPanInfoCard(Uri.parse(panData.panCardImagePath))
            } else {
                firebaseStorage
                        .reference
                        .child("verification")
                        .child(panData.panCardImagePath!!)
                        .downloadUrl.addOnSuccessListener {
                            showPanInfoCard(it)
                        }.addOnFailureListener {
                            print("ee")
                        }
            }
        }
    }

}