package com.gigforce.verification.gigerVerfication.panCard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.verification.gigerVerfication.*
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.GigerVerificationStatus
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.datamodels.verification.PanCardDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lse
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.core.utils.ImageSource
import com.gigforce.core.utils.SelectImageSourceBottomSheetActionListener
import com.gigforce.core.utils.VerificationValidations
import com.gigforce.verification.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_2.*
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_2.progressBar
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_2.toolbar
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_main_2.*
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_view_2.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.util.*
import javax.inject.Inject
@AndroidEntryPoint
class AddPanCardInfoFragment : Fragment(), SelectImageSourceBottomSheetActionListener,
    IOnBackPressedOverride {

    companion object {
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path"
        const val INTENT_EXTRA_PAN = "pan"
    }

    private var panCardDataModel: PanCardDataModel? = null
    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var gigerVerificationStatus: GigerVerificationStatus? = null

    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_add_pan_card_info_2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViews() {
        panImageHolder.setDocumentUploadLabel(getString(R.string.upload_pan_card_veri))
        panImageHolder.setDocumentUploadSubLabel(getString(R.string.please_upload_your_pan_veri))
//        panImageHolder.documentUploadLabelTV.text = getString(R.string.upload_pan_card)
//        panImageHolder.documentUploadSubLabelTV.text = getString(R.string.please_upload_your_pan)
        panSubmitSliderBtn.isEnabled = false

        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(resources, R.color.lipstick_2,null))
        toolbar.apply {
            hideActionMenu()
            showTitle(getString(R.string.giger_verification_veri))
            setBackButtonListener(View.OnClickListener {
                navigation.navigateTo("verification/main")
//                findNavController().popBackStack(R.id.gigerVerificationFragment, false)
            })
        }

        appBarComp.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.navigateTo("verification/main")
            })
        }

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

        //panImageHolder.uploadImageLayout.imageLabelTV.text = getString(R.string.pan_card_image)
        panImageHolder.uploadImageLabel(getString(R.string.pan_card_image_veri))

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

                    panViewLayout.gone()
                    panEditLayout.visible()

                    setDataOnEditLayout(panCardDataModel)
                    panCardAvailaibilityOptionRG.check(R.id.panYesRB)
                    panSubmitSliderBtn.isEnabled = false
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
                progressBar.gone()

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
        panEditLayout.gone()
        panViewLayout.visible()
        //TODO handle error message when process is ready

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
        progressBar.visibility = View.GONE
        panViewLayout.gone()
        panEditLayout.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_veri))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_veri)) { _, _ -> }
            .show()
    }

    private fun panCardDocumentUploaded() {
        showToast(getString(R.string.pan_details_uploaded_veri))
        gigerVerificationStatus?.let {

            if (!it.aadharCardDetailsUploaded) {
                navigation.navigateTo("verification/addAadharCardInfoFragment")
            } else if (!it.dlCardDetailsUploaded) {
                navigation.navigateTo("verification/addDrivingLicenseInfoFragment")
            } else if (!it.bankDetailsUploaded) {
                navigation.navigateTo("verification/addBankDetailsInfoFragment")
            } else /*if (!it.selfieVideoUploaded) {
                navigation.navigateTo("verification/addSelfieVideoFragment")
            } else */{
                showDetailsUploaded()
            }
        }
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
        panEditLayout.visibility = View.GONE
        panViewLayout.gone()
        progressBar.visibility = View.VISIBLE
    }

    override fun onBackPressed(): Boolean {
//        findNavController().popBackStack(R.id.gigerVerificationFragment, false)
        navigation.popBackStack("verification/main", inclusive = false)
        return true
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
        navigation.navigateToPhotoCrop(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE, requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_PAN_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPLOAD_PAN_IMAGE) {

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
    }

    private fun disableSubmitButton() {
        panSubmitSliderBtn.isEnabled = false
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        if (panDataCorrectCB.isChecked)
            enableSubmitButton()
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


}