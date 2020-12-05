package com.gigforce.app.modules.client_activation


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.VerificationValidations
import com.gigforce.app.modules.gigerVerfication.WhyWeNeedThisBottomSheet
import com.gigforce.app.modules.gigerVerfication.drivingLicense.AddDrivingLicenseInfoFragment
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseSides
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.*
import kotlinx.android.synthetic.main.layout_fragment_upload_driving_license_activation.*
import kotlinx.android.synthetic.main.upload_car_client_activation.view.*
import java.util.*

class UploadDrivingLicense : BaseFragment(), RejectionDialog.RejectionDialogCallbacks {

    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: GigVerificationViewModel by viewModels()

    private var dlFrontImagePath: Uri? = null
    private var dlBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.layout_fragment_upload_driving_license_activation, inflater, container)
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
            FROM_CLIENT_ACTIVATON = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)

        }

        arguments?.let {
            FROM_CLIENT_ACTIVATON = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)

        }
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            navFragmentsData?.setData(
                    bundleOf(
                            StringConstants.BACK_PRESSED.value to true

                    )
            )
        }
        return super.onBackPressed()
    }

    private fun initViews() {
        hideDLImageAndInfoLayout()
        disableSubmitButton()
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

        helpIconIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()
                ll_no_driving_license.gone()

                if ((dlFrontImagePath != null && dlBackImagePath != null)
                ) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.dlNoRB) {
                ll_no_driving_license.visible()
                hideDLImageAndInfoLayout()
                disableSubmitButton()


            } else {
                hideDLImageAndInfoLayout()
                disableSubmitButton()
            }
        }

        iv_back_application_client_activation.setOnClickListener {
            popBackState()
        }




        tv_action_upld_dl_cl_act.setOnClickListener {
            if (dlYesRB.isChecked) {

                if (stateSpinner.selectedItemPosition == 0) {
                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.select_dl_state))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
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

                    return@setOnClickListener
                }

                if ((dlFrontImagePath == null || dlBackImagePath == null)) {

                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.capture_both_sides_dl))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()

                    return@setOnClickListener
                }

                val state = stateSpinner.selectedItem.toString()

                viewModel.updateDLDataClientActivation(
                        true,
                        dlFrontImagePath,
                        dlBackImagePath,
                        state,
                        dlNo

                )

            } else if (dlNoRB.isChecked) {

                val rejectionDialog = RejectionDialog()
                rejectionDialog.setCallbacks(this)
                rejectionDialog
                rejectionDialog.show(parentFragmentManager, DrivingCertSuccessDialog::class.java.name)
//                viewModel.updateDLData(
//                        false,
//                        null,
//                        null,
//                        null,
//                        null
//                )
            }

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


    private fun initViewModel() {


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

}