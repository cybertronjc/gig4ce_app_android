package com.gigforce.verification.gigerVerfication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.verification.gigerVerfication.drivingLicense.AddDrivingLicenseInfoFragment
import com.gigforce.verification.gigerVerfication.drivingLicense.DrivingLicenseSides
import com.gigforce.verification.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lse
import com.gigforce.verification.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlAvailaibilityOptionRG
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlFrontImageHolder
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlNoRB
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.dlYesRB
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.helpIconIV
import kotlinx.android.synthetic.main.layout_driving_license_upload_client_activation.whyWeNeedThisTV
import kotlinx.android.synthetic.main.layout_fragment_upload_driving_certificate.*
import kotlinx.android.synthetic.main.layout_fragment_upload_driving_license_activation.dlMainLayout
import kotlinx.android.synthetic.main.layout_fragment_upload_driving_license_activation.iv_back_application_client_activation
import kotlinx.android.synthetic.main.layout_fragment_upload_driving_license_activation.progressBar
import kotlinx.android.synthetic.main.layout_upload_driving_certificate.*
import kotlinx.android.synthetic.main.upload_car_client_activation.view.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadDrivingCertificate : Fragment() {

    private lateinit var mTitle: String
    private lateinit var mType: String
    private lateinit var mJobProfileId: String
    private val viewModel: UploadDrivingCertificateViewmodel by viewModels()

    private var dlFrontImagePath: Uri? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null
    @Inject
    lateinit var navigation: INavigation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.layout_fragment_upload_driving_certificate,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)

        initViews()
        initViewModel()
        initClicks()

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }
    }

    private fun initClicks() {

        tv_schedule_test.setOnClickListener {
            navigation.popBackStack()
            navigation.navigateTo(
                "client_activation/doc_sub_doc", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to mJobProfileId,

                    StringConstants.TITLE.value to mTitle,
                    StringConstants.TYPE.value to mType
                )
            )
//            navigate(
//                R.id.fragment_doc_sub,
//                bundleOf(
//                    StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
//
//                    StringConstants.TITLE.value to mTitle,
//                    StringConstants.TYPE.value to mType
//                )
//            )
        }
    }

    private fun initViews() {
        hideDLImageAndInfoLayout()
        disableSubmitButton()
        dlFrontImageHolder.documentUploadLabelTV.text = getString(R.string.upload_driving_cert)
        dlFrontImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.please_upload_driving_cert)

        helpIconIV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        whyWeNeedThisTV.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()
                tv_schedule_test.gone()
                tv_action_upld_dl_cert.visible()
                ll_no_driving_license.gone()

                if ((dlFrontImagePath != null)
                ) {
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.dlNoRB) {
                hideDLImageAndInfoLayout()
                disableSubmitButton()
                tv_schedule_test.visible()
                tv_action_upld_dl_cert.gone()
                ll_no_driving_license.visible()


            } else {
                hideDLImageAndInfoLayout()
                disableSubmitButton()
                tv_schedule_test.gone()
                tv_action_upld_dl_cert.gone()
                ll_no_driving_license.gone()

            }
        }

        iv_back_application_client_activation.setOnClickListener {
            navigation.popBackStack()
        }
        appBarComp.setBackButtonListener(View.OnClickListener {
            navigation.popBackStack()
        })



        tv_action_upld_dl_cert.setOnClickListener {
            if (dlYesRB.isChecked) {
                if ((dlFrontImagePath == null)) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.capture_both_sides_dl))
                        .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                        .show()

                    return@setOnClickListener
                }


                viewModel.uploadDLCer(
                    mJobProfileId,
                    dlFrontImagePath, mType,
                    mTitle

                )

            } else if (dlNoRB.isChecked) {
                viewModel.uploadDLCer(
                    mJobProfileId,
                    null, mType,
                    mTitle


                )
            }

        }




        dlFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

//        dlBackImageHolder.uploadDocumentCardView.setOnClickListener {
//            openCameraAndGalleryOptionForBackSideImage()
//        }

        dlFrontImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.dl_image_front_side)

//        dlBackImageHolder.uploadImageLayout.imageLabelTV.text =
//                getString(R.string.dl_image_back_side)

        dlFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            openCameraAndGalleryOptionForFrontSideImage()
        }

//        dlBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
//            openCameraAndGalleryOptionForBackSideImage()
//        }


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

//        viewModel.getVerificationStatus()
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
        navigation.popBackStack()
    }


    private fun showLoadingState() {
        dlMainLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
            "purpose",
                "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "aadhar_card_front.jpg")
            navigation.navigateToPhotoCrop(photoCropIntent,
                AddDrivingLicenseInfoFragment.REQUEST_CODE_UPLOAD_DL,requireContext(),this)
//        startActivityForResult(
//            photoCropIntent,
//            AddDrivingLicenseInfoFragment.REQUEST_CODE_UPLOAD_DL
//        )

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
                "purpose",
                "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "aadhar_card_back.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,
                AddDrivingLicenseInfoFragment.REQUEST_CODE_UPLOAD_DL,requireContext(),this)
//        startActivityForResult(
//            photoCropIntent,
//            AddDrivingLicenseInfoFragment.REQUEST_CODE_UPLOAD_DL
//        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {

                if (DrivingLicenseSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    dlFrontImagePath =
                        data?.getParcelableExtra("uri")
                    showFrontDrivingLicense(dlFrontImagePath!!)
                }

//                else if (DrivingLicenseSides.BACK_SIDE == currentlyClickingImageOfSide) {
//                    dlBackImagePath =
//                            data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI)
//                    showBackDrivingLicense(dlBackImagePath!!)
//                }

                if (dlFrontImagePath != null

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
//        dlBackImageHolder.visibility = View.VISIBLE
        dlFrontImageHolder.visibility = View.VISIBLE
        showImageInfoLayout()
    }

    private fun hideDLImageAndInfoLayout() {
//        dlBackImageHolder.visibility = View.GONE
        dlFrontImageHolder.visibility = View.GONE
//        dlInfoLayout.visibility = View.GONE
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
//        dlInfoLayout.visibility = View.VISIBLE
    }


    private fun showFrontDrivingLicense(aadharFrontImagePath: Uri) {
        dlFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
        dlFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(aadharFrontImagePath)
            .placeholder(getCircularProgressDrawable())
            .into(dlFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

//    private fun showBackDrivingLicense(aadharBackImagePath: Uri) {
//        dlBackImageHolder.uploadDocumentCardView.visibility = View.GONE
//        dlBackImageHolder.uploadImageLayout.visibility = View.VISIBLE
//
//        Glide.with(requireContext())
//                .load(aadharBackImagePath)
//                .placeholder(getCircularProgressDrawable())
//                .into(dlBackImageHolder.uploadImageLayout.clickedImageIV)
//    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putString(StringConstants.TYPE.value, mType)
        outState.putString(StringConstants.TITLE.value, mTitle)


    }
}