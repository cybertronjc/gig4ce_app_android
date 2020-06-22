package com.gigforce.app.modules.gigerVerfication.aadharCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheet
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

enum class AadharCardSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddAadharCardInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    private val viewModel: GigVerificationViewModel by viewModels()

    private var aadharFrontImagePath: File? = null
    private var aadharBackImagePath: File? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_aadhar_card_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        aadharFrontImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_aadhar_card_front_side)
        aadharFrontImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_aadhar_card)

        aadharBackImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_aadhar_card_back_side)
        aadharBackImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_aadhar_card)

        aadharSubmitSliderBtn.isEnabled = false

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        aadharAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.aadharYesRB) {
                showAadharImageAndInfoLayout()
            } else {
                hideAadharImageAndInfoLayout()
                enableSubmitButton()
            }
        }

        aadharFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        aadharFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        aadharBackImageHolder.uploadDocumentCardView.setOnClickListener {
            currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        aadharBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        aadharEditLayout.setOnClickListener {
            navigate(R.id.editAadharInfoBottomSheet)
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

    private fun showImageInfoLayout() {
        aadharInfoLayout.visibility = View.VISIBLE
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        if (currentlyClickingImageOfSide == null)
            return
        else if (currentlyClickingImageOfSide == AadharCardSides.BACK_SIDE) {
            aadharBackImagePath = File("ma")
            showBackAadharCard(aadharBackImagePath)
            enableSubmitButton()
        } else if (currentlyClickingImageOfSide == AadharCardSides.FRONT_SIDE) {
            aadharFrontImagePath = File("ma")
            showFrontAadharCard(aadharFrontImagePath)
            enableSubmitButton()
        }

        setAadharInfoOnView(
            name = "Rahul Jain",
            dob = "11/09/1990",
            gender = "Male",
            aadharNo = "2345 7624 9238",
            address = "House no 3601, PT-Vihar, New Delhi, Delhi 110033"
        )
    }

    private fun showFrontAadharCard(aadharFrontImagePath: File? = null) {
        aadharFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
        aadharFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE
        aadharFrontImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_aadhar_card_front_side)

        Glide.with(requireContext())
            .load(R.drawable.bg_aadhar_front_placeholder)
            .into(aadharFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun showBackAadharCard(aadharBackImagePath: File? = null) {
        aadharBackImageHolder.uploadDocumentCardView.visibility = View.GONE
        aadharBackImageHolder.uploadImageLayout.visibility = View.VISIBLE
        aadharBackImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_aadhar_card_back_side)

        Glide.with(requireContext())
            .load(R.drawable.bg_aadhar_front_placeholder)
            .into(aadharBackImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun setAadharInfoOnView(
        name: String?,
        dob: String?,
        gender: String?,
        aadharNo: String?,
        address: String?
    ) {
        nameTV.text = name
        dobTV.text = dob
        genderTV.text = gender
        aadharNoTV.text = aadharNo
        addressTV.text = address
    }

}