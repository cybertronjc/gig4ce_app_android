package com.gigforce.verification.gigerVerfication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.gigforce.core.datamodels.verification.PanCardDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.ImageSource
import com.gigforce.core.utils.Lse
import com.gigforce.core.utils.SelectImageSourceBottomSheetActionListener
import com.gigforce.core.utils.VerificationValidations
import com.gigforce.verification.R
import com.gigforce.verification.gigerVerfication.panCard.AddPanCardInfoFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.android.synthetic.main.fragment_add_pan_card_info_2.*
//import kotlinx.android.synthetic.main.fragment_add_pan_card_info_2.panViewLayout
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_main_2.*
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_view_2.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import kotlinx.android.synthetic.main.upload_pan_card_fragment.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadPanCard : Fragment(), SelectImageSourceBottomSheetActionListener,
    IOnBackPressedOverride {

    companion object {
        fun newInstance() = UploadPanCard()
    }

    private lateinit var mTitle: String
    private lateinit var mType: String
    private lateinit var mJobProfileId: String
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: GigVerificationViewModel by viewModels()
    private var panCardDataModel: PanCardDataModel? = null
    private var clickedImagePath: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    @Inject
    lateinit var navigation: INavigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.upload_pan_card_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initViews()
        initViewModel()

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

    private fun initViews() {
        panImageHolder.setDocumentUploadLabel(getString(R.string.upload_pan_card_veri))
        panImageHolder.setDocumentUploadSubLabel(getString(R.string.please_upload_your_pan_veri))
//        panImageHolder.documentUploadLabelTV.text = getString(R.string.upload_pan_card)
//        panImageHolder.documentUploadSubLabelTV.text = getString(R.string.please_upload_your_pan)
        panSubmitSliderBtn.isEnabled = false
        iv_back_application_client_activation.setOnClickListener {
            onBackPressed()
        }
        appBarComp.setBackButtonListener(View.OnClickListener {
            onBackPressed()
        })


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

        panImageHolder.uploadDocumentCardView.setOnClickListener {
            launchSelectImageSourceDialog()
        }

        panImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            launchSelectImageSourceDialog()
        }

        panImageHolder.uploadImageLayout.imageLabelTV.text = getString(R.string.pan_card_image_veri)

        panCardAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.panYesRB) {
                showPanImageLayout()
                showImageInfoLayout()

                if (panDataCorrectCB.isChecked && (panSubmitSliderBtn.text == getString(R.string.update_veri)
                            || clickedImagePath != null)
                ) {
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

                if (panYesRB.isChecked && (panSubmitSliderBtn.text == getString(R.string.update_veri) || clickedImagePath != null))
                    enableSubmitButton()
                else if (panNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
        }

        editLayout.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert_veri))
                .setMessage(getString(R.string.you_are_reuploading_pan_card_veri))
                .setPositiveButton(getString(R.string.okay_veri)) { _, _ ->

                    panViewLayout1.gone()
                    panMainLayout1.visible()

                    setDataOnEditLayout(panCardDataModel)
                    panCardAvailaibilityOptionRG.check(R.id.panYesRB)
                    panSubmitSliderBtn.isEnabled = true
                }
                .setNegativeButton(getString(R.string.cancel_veri)) { _, _ -> }
                .show()
        }

        panSubmitSliderBtn.setOnClickListener {
                    if (panYesRB.isChecked || panSubmitSliderBtn.text == getString(R.string.update_veri)) {
                        val panCardNo =
                            panCardEditText.text.toString().toUpperCase(Locale.getDefault())
                        if (!VerificationValidations.isPanCardValid(panCardNo)) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.enter_valid_pan_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()

                            return@setOnClickListener
                        }

                        if (panSubmitSliderBtn.text != getString(R.string.update_veri) && clickedImagePath == null) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.alert_veri))
                                .setMessage(getString(R.string.click_select_pan_image_veri))
                                .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
                                .show()
                            return@setOnClickListener
                        }

                        viewModel.updatePanImagePath(true, clickedImagePath, panCardNo)
                    } else if (panNoRB.isChecked) {
                        viewModel.updatePanImagePath(false, null, null)
                    }
                }

    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_do_we_need_this_pan_veri)
        )
    }


    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                this.gigerVerificationStatus = it
                this.panCardDataModel = it.panCardDataModel
                progressBar1.gone()

                if (it.panCardDetailsUploaded && it.panCardDataModel != null) {

                    if (it.panCardDataModel!!.userHasPanCard != null) {
                        if (it.panCardDataModel!!.userHasPanCard!!) {
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

        viewModel.getVerificationStatus()
    }


    private fun setDataOnViewLayout(gigVerificationStatus: GigerVerificationStatus) {
        panMainLayout1.gone()
        panViewLayout1.visible()

        val panDetails = gigVerificationStatus.panCardDataModel ?: return

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
                Glide.with(requireContext()).load(panDetails.panCardImagePath)
                    .placeholder(getCircularProgressDrawable()).into(panViewImageIV)
            } else {
                firebaseStorage
                    .reference
                    .child("verification")
                    .child(panDetails.panCardImagePath!!)
                    .downloadUrl.addOnSuccessListener {
                        Glide.with(requireContext()).load(it)
                            .placeholder(getCircularProgressDrawable()).into(panViewImageIV)
                    }.addOnFailureListener {
                        print("ee")
                    }
            }
        }
        panViewImageErrorMessage.gone()

        panViewNoTV.text = panDetails.panCardNo
        panViewNoErrorMessage.gone()
    }

    private fun setDataOnEditLayout(it: PanCardDataModel?) {
        panViewLayout1.gone()
        panMainLayout1.visible()

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
        panSubmitSliderBtn.text = getString(R.string.update_veri)

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

    private fun errorOnUploadingDocuments(error: String) {
        progressBar1.visibility = View.GONE
        panViewLayout1.gone()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_veri))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
            .show()
    }

    private fun panCardDocumentUploaded() {
        showToast(getString(R.string.pan_details_uploaded_veri))
        navigation.popBackStack()
    }


//    private fun showDetailsUploaded() {
//        val view =
//            layoutInflater.inflate(R.layout.fragment_giger_verification_documents_submitted, null)
//
//        val dialog = AlertDialog.Builder(requireContext())
//            .setView(view)
//            .show()
//
//        view.findViewById<View>(R.id.verificationCompletedBtn)
//            .setOnClickListener {
//                dialog.dismiss()
//                navigation.popBackStack("verification/main",inclusive = false)
////                findNavController().popBackStack(R.id.gigerVerificationFragment, false)
//            }
//    }

    private fun showLoadingState() {
        panViewLayout1.visible()
        progressBar1.visible()
    }



    private fun launchSelectImageSourceDialog() {
//        SelectImageSourceBottomSheet.launch(
//            childFragmentManager = childFragmentManager,
//            selectImageSourceBottomSheetActionListener = this
//        )

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra("purpose", "verification")
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "pan_card.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,
            AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE, requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {
                clickedImagePath =
                    data?.getParcelableExtra("uri")
                showPanInfoCard(clickedImagePath!!)

                if (panDataCorrectCB.isChecked)
                    enableSubmitButton()

                if (clickedImagePath != null && panSubmitSliderBtn.isGone) {
                    panSubmitSliderBtn.visible()
                    panDataCorrectCB.visible()
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

    private fun showPanImageLayout() {
        panImageHolder.visibility = View.VISIBLE
    }

    private fun hidePanImageAndInfoLayout() {
        panImageHolder.visibility = View.GONE
        panInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        panSubmitSliderBtn.isEnabled = true

    }

    private fun disableSubmitButton() {
        panSubmitSliderBtn.isEnabled = false
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        if (panDataCorrectCB.isChecked)
            enableSubmitButton()
    }
    private fun showImageInfoLayout() {
        panInfoLayout.visibility = View.VISIBLE
    }

    private fun showPanInfoCard(panInfoPath: Uri) {
//        panImageHolder.uploadDocumentCardView.visibility = View.GONE
//        panImageHolder.uploadImageLayout.visibility = View.VISIBLE
//
//        Glide.with(requireContext())
//            .load(panInfoPath)
//            .placeholder(getCircularProgressDrawable())
//            .into(panImageHolder.uploadImageLayout.clickedImageIV)
        panImageHolder.makeEditLayoutVisible()
        panImageHolder.setImage(panInfoPath)
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

}