package com.gigforce.app.modules.gigerVerfication.aadharCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import kotlinx.android.synthetic.main.fragment_add_aadhar_card_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

class AddAadharCardInfoFragment : BaseFragment() {

    private val viewModel: GigVerificationViewModel by viewModels()

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

        aadharAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.aadharYesRB) {
                showAadharImageAndInfoLayout()
            } else {
                hideAadharImageAndInfoLayout()
                enableSubmitButton()
            }
        }

        aadharEditLayout.setOnClickListener {
            findNavController().navigate(R.id.editAadharInfoBottomSheet)
        }
    }

    private fun showAadharImageAndInfoLayout() {
        aadharBackImageHolder.visibility = View.VISIBLE
        aadharBackImageHolder.visibility = View.VISIBLE
        aadharInfoLayout.visibility = View.VISIBLE
    }

    private fun hideAadharImageAndInfoLayout() {
        aadharBackImageHolder.visibility = View.GONE
        aadharBackImageHolder.visibility = View.GONE
        aadharInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        aadharSubmitSliderBtn.isEnabled = true
    }

}