package com.gigforce.verification.gigerVerfication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.GigerVerificationStatus
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.datamodels.verification.AadharCardDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lse
import com.gigforce.verification.R
import com.gigforce.verification.gigerVerfication.aadharCard.AadharCardSides
import com.gigforce.verification.gigerVerfication.aadharCard.AddAadharCardInfoFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_info_main_2.*
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_info_main_2.topSeaparator
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_info_main_2.whyWeNeedThisTV
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_view_2.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import kotlinx.android.synthetic.main.upload_aadhar_card_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadAadharCard : Fragment(), IOnBackPressedOverride {

    companion object {
        fun newInstance() = UploadAadharCard()
    }

    private lateinit var mTitle: String
    private lateinit var mType: String
    private lateinit var mJobProfileId: String
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: GigVerificationViewModel by viewModels()
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var aadharCardDataModel: AadharCardDataModel? = null
    private var aadharFrontImagePath: Uri? = null
    private var aadharBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null
    @Inject
    lateinit var navigation: INavigation



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.upload_aadhar_card_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initViews()
        initViewModel()
        initClicks()
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


    private fun initClicks() {
        aadharFrontImageHolder.setDocumentUploadLabel(getString(R.string.upload_aadhar_card_front_side_veri))
        aadharFrontImageHolder.setDocumentUploadSubLabel(getString(R.string.upload_your_aadhar_card_veri))

//        aadharFrontImageHolder.documentUploadLabelTV.text =
//            getString(R.string.upload_aadhar_card_front_side)
//        aadharFrontImageHolder.documentUploadSubLabelTV.text =
//            getString(R.string.upload_your_aadhar_card)

        aadharBackImageHolder.setDocumentUploadLabel(getString(R.string.upload_aadhar_card_front_side_veri))
        aadharBackImageHolder.setDocumentUploadSubLabel(getString(R.string.upload_your_aadhar_card_veri))

//        aadharBackImageHolder.documentUploadLabelTV.text =
//            getString(R.string.upload_aadhar_card_back_side)
//        aadharBackImageHolder.documentUploadSubLabelTV.text =
//            getString(R.string.upload_your_aadhar_card)

        aadharSubmitSliderBtn.isEnabled = false

        iv_back_upload_aadhar_card_client_activation.setOnClickListener {
            onBackPressed()
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

                if (aadharDataCorrectCB.isChecked
                    && ((aadharSubmitSliderBtn.text == getString(R.string.update_veri)
                            || (aadharFrontImagePath != null && aadharBackImagePath != null)))
                ) {
                    enableSubmitButton()
                } else {
                    disableSubmitButton()
                }

            } else if (checkedId == R.id.aadharNoRB) {
                hideAadharImageAndInfoLayout()

                aadharSubmitSliderBtn.visible()
                aadharDataCorrectCB.visible()

                if (aadharDataCorrectCB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()

            } else {
                hideAadharImageAndInfoLayout()
                disableSubmitButton()
            }
        }

        aadharDataCorrectCB.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                if (aadharYesRB.isChecked && ((aadharSubmitSliderBtn.text == getString(R.string.update_veri)
                            || (aadharFrontImagePath != null && aadharBackImagePath != null)))
                )
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


        aadharSubmitSliderBtn.setOnClickListener {
                    if (aadharYesRB.isChecked || aadharSubmitSliderBtn.text == getString(R.string.update_veri)) {
                        if (aadharCardET.text!!.length != 12) {

                            aadharMainLayout.post {
                                aadharMainLayout.scrollTo(0, topSeaparator.y.toInt())
                            }

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.enter_valid_aadhar_no_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }


                        if (aadharSubmitSliderBtn.text != getString(R.string.update_veri) && (aadharFrontImagePath == null || aadharBackImagePath == null)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.select_or_capture_both_sides_of_aadhar_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

                        val aadharNo = aadharCardET.text.toString()

                        viewModel.updateAadharData(
                            true,
                            aadharFrontImagePath,
                            aadharBackImagePath,
                            aadharNo
                        )

                    } else if (aadharNoRB.isChecked) {
                        viewModel.updateAadharData(false, null, null, null)
                    }
                }



        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert_veri))
                .setMessage(getString(R.string.you_are_reuploading_aadhar_veri))
                .setPositiveButton(getString(R.string.okay_veri)) { _, _ ->

                    aadharViewLayout1.gone()
                    aadharMainLayout.visible()

                    setDataOnEditLayout(aadharCardDataModel)
                    aadharAvailaibilityOptionRG.check(R.id.aadharYesRB)
                    aadharSubmitSliderBtn.isEnabled = true
                }
                .setNegativeButton(getString(R.string.cancel_veri)) { _, _ -> }
                .show()
        }
    }

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()


    private fun showWhyWeNeedThisBottomSheet() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_we_need_this_aadhar_veri)
        )
    }

    private fun initViewModel() {

        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                this.gigerVerificationStatus = it
                this.aadharCardDataModel = it.aadharCardDataModel
                progressBar1.gone()

                if (it.aadharCardDetailsUploaded && it.aadharCardDataModel != null) {

                    if (it.aadharCardDataModel!!.userHasAadharCard != null) {
                        if (it.aadharCardDataModel!!.userHasAadharCard!!) {
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

        viewModel.getVerificationStatus()

    }

    private fun initViews() {

    }

    private fun documentUploaded() {
        Log.d("showing", "Document uploaded")
        showToast(getString(R.string.aadhar_card_details_uploaded_veri))
        navigation.popBackStack()
    }

    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        aadharMainLayout.gone()
        aadharViewLayout1.visible()
        //TODO handle error message when process is ready

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
            if (aadharDetails.frontImage!!.startsWith("http", true)) {
                Glide.with(requireContext()).load(aadharDetails.frontImage)
                    .placeholder(getCircularProgressDrawable()).into(aadharViewFrontImageIV)
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(aadharDetails.frontImage!!)
                    .downloadUrl.addOnSuccessListener {
                        Glide.with(requireContext()).load(it)
                            .placeholder(getCircularProgressDrawable()).into(aadharViewFrontImageIV)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }
        aadharViewFrontErrorMessage.gone()

        if (aadharDetails.backImage != null) {
            if (aadharDetails.backImage!!.startsWith("http", true)) {
                Glide.with(requireContext()).load(aadharDetails.backImage)
                    .placeholder(getCircularProgressDrawable()).into(aadharViewBackImageIV)
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(aadharDetails.backImage!!)
                    .downloadUrl.addOnSuccessListener {
                        Glide.with(requireContext()).load(it)
                            .placeholder(getCircularProgressDrawable()).into(aadharViewBackImageIV)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }
        aadharViewBackErrorMessageTV.gone()

        aadharNoTV.text = aadharDetails.aadharCardNo
        aadharNumberViewErrorMessage.gone()
    }


    private fun setDataOnEditLayout(it: AadharCardDataModel?) {
        aadharViewLayout1.gone()
        aadharMainLayout.visible()

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
        aadharSubmitSliderBtn.text = getString(R.string.update_veri)

        aadharCardET.setText(aadharData.aadharCardNo)


        if (aadharData.frontImage != null) {
            if (aadharData.frontImage!!.startsWith("http", true)) {
                showFrontAadharCard(Uri.parse(aadharData.frontImage))
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(aadharData.frontImage!!)
                    .downloadUrl.addOnSuccessListener {
                        showFrontAadharCard(it)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }

        if (aadharData.backImage != null) {
            if (aadharData.backImage!!.startsWith("http", true)) {
                showBackAadharCard(Uri.parse(aadharData.backImage))
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(aadharData.backImage!!)
                    .downloadUrl.addOnSuccessListener {
                        showBackAadharCard(it)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }
    }

    private fun errorOnUploadingDocuments(error: String) {
        progressBar1.visibility = View.GONE
        aadharViewLayout1.gone()
        aadharMainLayout.visibility = View.VISIBLE

        Log.d("showing", "error with layout")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_veri))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
            .show()
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
                navigation.popBackStack("verification/main",inclusive = false)
//                findNavController().popBackStack(R.id.gigerVerificationFragment, false)
            }
    }

    private fun showLoadingState() {
        Log.d("showing", "loading with layout")
        aadharMainLayout.visibility = View.VISIBLE
        progressBar1.visibility = View.VISIBLE
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            var navFragmentsData = activity as NavFragmentsData
            navFragmentsData.setData(
                bundleOf(
                    StringConstants.BACK_PRESSED.value to true

                )
            )
            navigation.popBackStack()
            return true
        }
        return false
    }


    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE

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
            AddAadharCardInfoFragment.REQUEST_CODE_UPLOAD_AADHAR_IMAGE, requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE

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
            AddAadharCardInfoFragment.REQUEST_CODE_UPLOAD_AADHAR_IMAGE, requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddAadharCardInfoFragment.REQUEST_CODE_UPLOAD_AADHAR_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {

                if (AadharCardSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    aadharFrontImagePath =
                        data?.getParcelableExtra("uri")
                    showFrontAadharCard(aadharFrontImagePath!!)
                } else if (AadharCardSides.BACK_SIDE == currentlyClickingImageOfSide) {
                    aadharBackImagePath =
                        data?.getParcelableExtra("uri")
                    showBackAadharCard(aadharBackImagePath!!)
                }

                if (aadharDataCorrectCB.isChecked
                    && aadharFrontImagePath != null
                    && aadharBackImagePath != null
                ) {
                    enableSubmitButton()
                } else {
                    disableSubmitButton()
                }

                if (aadharFrontImagePath != null && aadharBackImagePath != null && aadharSubmitSliderBtn.isGone) {
                    aadharSubmitSliderBtn.visible()
                    aadharDataCorrectCB.visible()
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

    }

    private fun disableSubmitButton() {
        aadharSubmitSliderBtn.isEnabled = false

    }

    private fun showImageInfoLayout() {
        aadharInfoLayout.visibility = View.VISIBLE
    }


    private fun showFrontAadharCard(aadharFrontImagePath: Uri) {

//        aadharFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
//        aadharFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE
//        aadharFrontImageHolder.uploadImageLayout.imageLabelTV.text =
//            getString(R.string.aadhar_card_front_image)
//
//        Glide.with(requireContext())
//            .load(aadharFrontImagePath)
//            .placeholder(getCircularProgressDrawable())
//            .into(aadharFrontImageHolder.uploadImageLayout.clickedImageIV)
        aadharFrontImageHolder.makeEditLayoutVisible()
        aadharFrontImageHolder.uploadImageLabel(getString(R.string.aadhar_card_front_image_veri))
        aadharFrontImageHolder.setImage(aadharFrontImagePath)
    }

    private fun showBackAadharCard(aadharBackImagePath: Uri) {
//        aadharBackImageHolder.uploadDocumentCardView.visibility = View.GONE
//        aadharBackImageHolder.uploadImageLayout.visibility = View.VISIBLE
//        aadharBackImageHolder.uploadImageLayout.imageLabelTV.text =
//            getString(R.string.aadhar_card_back_image)
//
//        Glide.with(requireContext())
//            .load(aadharBackImagePath)
//            .placeholder(getCircularProgressDrawable())
//            .into(aadharBackImageHolder.uploadImageLayout.clickedImageIV)
        aadharBackImageHolder.makeUploadLayoutVisible()
        aadharBackImageHolder.uploadImageLabel(getString(R.string.aadhar_card_back_image_veri))

        aadharBackImageHolder .setImage(aadharBackImagePath)
    }

}