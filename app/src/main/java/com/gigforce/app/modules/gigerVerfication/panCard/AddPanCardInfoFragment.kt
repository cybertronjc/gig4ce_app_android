package com.gigforce.app.modules.gigerVerfication.panCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheet
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import kotlinx.android.synthetic.main.fragment_add_pan_card_info.*
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

        panImageHolder.uploadDocumentCardView.setOnClickListener {
            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

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
    }

    private fun disableSubmitButton() {
        panSubmitSliderBtn.isEnabled = false
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        clickedImagePath = File("na")
        enableSubmitButton()
        setPanInfoOnView(
            name = "Rahul Jr",
            fathersName = "Rahul Sr",
            dob = "11/09/1990",
            pan = "PU23SDDLOJIJ"
        )
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