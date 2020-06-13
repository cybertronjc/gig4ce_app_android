package com.gigforce.app.modules.gigerVerfication.bankDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import kotlinx.android.synthetic.main.fragment_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

class AddBankDetailsInfoFragment : BaseFragment() {

    private val viewModel: GigVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_bank_details_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        passbookImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_bank_passbook)
        passbookImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_bank_passbook_sublabel)

        passbookSubmitSliderBtn.isEnabled = false

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageAndInfoLayout()
            } else {
                hidePassbookImageAndInfoLayout()
                enableSubmitButton()
            }
        }

        editBankDetailsLayout.setOnClickListener {
            findNavController().navigate(R.id.editBankDetailsInfoBottomSheet)
        }
    }

    private fun showPassbookImageAndInfoLayout() {
        passbookImageHolder.visibility = View.VISIBLE
        passbookInfoLayout.visibility = View.VISIBLE
    }

    private fun hidePassbookImageAndInfoLayout() {
        passbookImageHolder.visibility = View.GONE
        passbookInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        passbookSubmitSliderBtn.isEnabled = true
    }

}