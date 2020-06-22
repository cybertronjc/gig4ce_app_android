package com.gigforce.app.modules.gigerVerfication.panCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheet
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import kotlinx.android.synthetic.main.fragment_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.*
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.fathersNameTV
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.nameTV
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.toolbar
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

class AddPanCardInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_pan_card_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        panImageHolder.documentUploadLabelTV.text = getString(R.string.upload_pan_card)
        panImageHolder.documentUploadSubLabelTV.text = getString(R.string.please_upload_your_pan)
        panSubmitSliderBtn.isEnabled = false

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        panImageHolder.uploadDocumentCardView.setOnClickListener {
            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        panImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        panImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_pan_card)

        panCardAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.panYesRB) {
                showPanImageLayout()

                if (clickedImagePath != null) {
                    showImageInfoLayout()
                    enableSubmitButton()
                } else
                    disableSubmitButton()
            } else {
                hidePanImageAndInfoLayout()
                enableSubmitButton()
            }
        }

        panDataCorrectCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                enableSubmitButton()
            } else
                disableSubmitButton()
        }

        editPanInfoLayout.setOnClickListener {
            navigate(R.id.editPanInfoBottomSheet)
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
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        clickedImagePath = File("na")
        enableSubmitButton()
        showPanInfoCard(clickedImagePath)
        setPanInfoOnView(
            name = "Rahul Jain",
            fathersName = "Sahil Jain",
            dob = "11/09/1990",
            pan = "PU23SDDLOJIJ"
        )
    }

    private fun showPanInfoCard(panInfoPath: File? = null) {
        panImageHolder.uploadDocumentCardView.visibility = View.GONE
        panImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(R.drawable.bg_pan_card)
            .into(panImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setPanInfoOnView(
        name: String?,
        fathersName: String?,
        dob: String?,
        pan: String?
    ) {
        nameTV.text = name
        fathersNameTV.text = fathersName
        dobTV.text = dob
        panNoTV.text = pan
    }

}