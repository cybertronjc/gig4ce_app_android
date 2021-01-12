package com.gigforce.app.modules.client_activation


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
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
import com.gigforce.app.modules.gigerVerfication.drivingLicense.AddDrivingLicenseInfoFragment
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseSides
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.GenericSpinnerAdapter
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_add_driving_license_info_main.*
import kotlinx.android.synthetic.main.fragment_add_driving_license_info_view.*
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.*
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlAvailaibilityOptionRG
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlBackImageHolder
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlFrontImageHolder
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlInfoLayout
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlNoRB
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlYesRB
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.drivingLicenseEditText
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.helpIconIV
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.stateSpinner
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.whyWeNeedThisTV
import kotlinx.android.synthetic.main.layout_fragment_upload_driving_license_activation.*
import kotlinx.android.synthetic.main.upload_car_client_activation.view.*
import kotlinx.android.synthetic.main.upload_car_client_activation.view.imageLabelTV
import java.util.*

class UploadDrivingLicense : BaseFragment(), RejectionDialog.RejectionDialogCallbacks {

    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: GigVerificationViewModel by viewModels()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private var dlFrontImagePath: Uri? = null
    private var dlBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null
    private var drivingLicenseDetail: DrivingLicenseDataModel? = null
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(
                R.layout.layout_fragment_upload_driving_license_activation,
                inflater,
                container
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initViews()
        initViewModel()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)

        }

        arguments?.let {
            FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)

        }
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            navFragmentsData?.setData(
                    bundleOf(
                            StringConstants.BACK_PRESSED.value to true

                    )
            )
            popBackState()
            return true
        }
        return super.onBackPressed()
    }

    private fun initViews() {
        hideDLImageAndInfoLayout()
        val adapter =
                GenericSpinnerAdapter<String>(
                        requireContext(),
                        R.layout.layout_sp_driving_license_state,
                        resources.getStringArray(R.array.indian_states).toList()
                )
        stateSpinner.adapter = adapter

        iv_back_application_client_activation.setOnClickListener {
            onBackPressed()
        }

        dlFrontImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_driving_license_front_side)
        dlFrontImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_your_driving_license)

        dlBackImageHolder.documentUploadLabelTV.text =
                getString(R.string.upload_driving_license_back_side)
        dlBackImageHolder.documentUploadSubLabelTV.text =
                getString(R.string.upload_your_driving_license)
//        dlSubmitSliderBtn.isEnabled = false

//        toolbar.setNavigationOnClickListener {
//            findNavController().popBackStack(R.id.gigerVerificationFragment, false)
//        }

        helpIconViewIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisViewTV.setOnClickListener {
            showWhyWeNeedThisDialog()
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

                if ((dlFrontImagePath != null && dlBackImagePath != null)) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.dlNoRB) {
                hideDLImageAndInfoLayout()

//                dlSubmitSliderBtn.visible()
//                confirmDLDataCB.visible()

//                if (confirmDLDataCB.isChecked)
//                    enableSubmitButton()
//                else
//                    disableSubmitButton()

            } else {
                hideDLImageAndInfoLayout()
                disableSubmitButton()
            }
        }

//        confirmDLDataCB.setOnCheckedChangeListener { _, isChecked ->
//
//            if (isChecked) {
//
//                if (dlYesRB.isChecked
//                        && ((dlSubmitSliderBtn.text == getString(R.string.update)
//                                || (dlFrontImagePath != null && dlBackImagePath != null)))
//                )
//                    enableSubmitButton()
//                else if (dlNoRB.isChecked)
//                    enableSubmitButton()
//                else
//                    disableSubmitButton()
//            } else
//                disableSubmitButton()
//        }
        tv_action_upld_dl_cl_act.setOnClickListener {

            if (dlYesRB.isChecked) {

                if (stateSpinner.selectedItemPosition == 0) {
                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.select_dl_state))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
//                    dlSubmitSliderBtn.resetSlider()
                    return@setOnClickListener
                }

                val dlNo =
                        drivingLicenseEditText.text.toString().toUpperCase(Locale.getDefault())
                if (!VerificationValidations.isDLNumberValid(dlNo)) {

                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.enter_valid_dl))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()

//                    dlSubmitSliderBtn.resetSlider()
                    return@setOnClickListener
                }

                if ((dlFrontImagePath == null || dlBackImagePath == null)) {

                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.capture_both_sides_dl))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
//                    dlSubmitSliderBtn.resetSlider()
                    return@setOnClickListener
                }

                val state = stateSpinner.selectedItem.toString()

                viewModel.updateDLData(
                        true,
                        dlFrontImagePath,
                        dlBackImagePath,
                        state,
                        dlNo
                )

            } else if (dlNoRB.isChecked) {
                val rejectionDialog = RejectionDialog()
                rejectionDialog.setCallbacks(this@UploadDrivingLicense)
                rejectionDialog
                rejectionDialog.show(
                        parentFragmentManager,
                        RejectionDialog::class.java.name
                )
            }
        }

//        dlSubmitSliderBtn.onSlideCompleteListener =
//                object : SlideToActView.OnSlideCompleteListener {
//
//                    override fun onSlideComplete(view: SlideToActView) {
//
//                    }
//                }


        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.reuploading_driving_license))
                    .setPositiveButton(getString(R.string.okay)) { _, _ ->

                        dlViewLayout_client_activation.gone()
                        dlMainLayout.visible()

                        setDataOnEditLayout(drivingLicenseDetail)
//                        dlSubmitSliderBtn.isEnabled = true
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


    //    private fun initViewModel() {
//
//
//        viewModel.documentUploadState
//            .observe(viewLifecycleOwner, Observer {
//                when (it) {
//                    Lse.Loading -> showLoadingState()
//                    Lse.Success -> documentUploaded()
//                    is Lse.Error -> errorOnUploadingDocuments(it.error)
//                }
//            })
//
//        viewModel.getVerificationStatus()
//    }
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

        viewModel.getVerificationStatus()
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
                AddDrivingLicenseInfoFragment.REQUEST_CODE_UPLOAD_DL
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
                AddDrivingLicenseInfoFragment.REQUEST_CODE_UPLOAD_DL
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

                if (dlFrontImagePath != null
                        && dlBackImagePath != null
                ) {
                    enableSubmitButton()
                }


            }

//            else {
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle(getString(R.string.alert))
//                    .setMessage(getString(R.string.unable_to_capture_image))
//                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
//                    .show()
//            }
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
//        tv_action_upld_dl_cl_act.isEnabled = true
//        tv_action_upld_dl_cl_act.alpha = 1f

    }

    private fun disableSubmitButton() {
//        tv_action_upld_dl_cl_act.isEnabled = false
//        tv_action_upld_dl_cl_act.alpha = 0.5f

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

    override fun onClickRefer() {
        navigate(R.id.referrals_fragment)
    }

    override fun onClickTakMeHome() {
        findNavController().popBackStack(R.id.landinghomefragment, true)
    }


    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        dlMainLayout.gone()
        dlViewLayout_client_activation.visible()
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


    private fun setDataOnEditLayout(it: DrivingLicenseDataModel?) {
        dlViewLayout_client_activation.gone()
        dlMainLayout.visible()

        if (it != null) {
            //Fill previous data
            dlAvailaibilityOptionRG.gone()
            doYouHaveDLLabel_client_act.gone()
        } else {
            dlAvailaibilityOptionRG.visible()
            doYouHaveDLLabel_client_act.visible()

            dlEditOverallErrorMessage.gone()
            dlStateEditErrorMessage_client_act.gone()
            dlNoEditErrorMessage_client_act.gone()
            dlFrontEditErrorMessage.gone()
            dlBackEditErrorMessage.gone()
        }

        val dlData = it ?: return
//        dlSubmitSliderBtn.text = getString(R.string.update)

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

    private fun errorOnUploadingDocuments(error: String) {
        progressBar.visibility = View.GONE
        dlMainLayout.visibility = View.VISIBLE
//        dlSubmitSliderBtn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(error)
                .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                .show()
    }

    private fun documentUploaded() {
        showToast(getString(R.string.dl_details_uploaded))
        popBackState()
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

}