package com.gigforce.app.modules.gigerVerfication.bankDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheet
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import kotlinx.android.synthetic.main.fragment_add_bank_details_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

class AddBankDetailsInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    private val viewModel: GigVerificationViewModel by viewModels()
    private var clickedImagePath: File? = null

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

        passbookImageHolder.uploadDocumentCardView.setOnClickListener {
            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        passbookAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.passbookYesRB) {
                showPassbookImageLayout()

                if (clickedImagePath != null && bankDetailsDataConfirmationCB.isChecked) {
                    showPassbookInfoLayout()
                    enableSubmitButton()
                } else
                    disableSubmitButton()

            } else if (checkedId == R.id.passbookNoRB && bankDetailsDataConfirmationCB.isChecked) {
                hidePassbookImageAndInfoLayout()
                enableSubmitButton()
            } else {
                hidePassbookImageAndInfoLayout()
                disableSubmitButton()
            }
        }

        bankDetailsDataConfirmationCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (passbookAvailaibilityOptionRG.checkedRadioButtonId == R.id.passbookNoRB)
                    enableSubmitButton()
                else if (passbookAvailaibilityOptionRG.checkedRadioButtonId == R.id.passbookYesRB && clickedImagePath != null)
                    enableSubmitButton()
                else
                    disableSubmitButton()

            } else
                disableSubmitButton()
        }

        editBankDetailsLayout.setOnClickListener {
            navigate(R.id.editBankDetailsInfoBottomSheet)
        }
    }

    private fun disableSubmitButton() {
        passbookSubmitSliderBtn.isEnabled = false

        passbookSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        passbookSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
    }

    private fun showPassbookImageLayout() {
        passbookImageHolder.visibility = View.VISIBLE
    }

    private fun showPassbookInfoLayout() {
        passbookInfoLayout.visibility = View.VISIBLE
    }

    private fun hidePassbookImageAndInfoLayout() {
        passbookImageHolder.visibility = View.GONE
        passbookInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        passbookSubmitSliderBtn.isEnabled = true

        passbookSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        passbookSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showPassbookInfoLayout()
        bankDetailsDataConfirmationCB.visibility = View.VISIBLE

        clickedImagePath = File("na")
        setBankDetailsInfoOnView(
            name = "Rahul Jain",
            fathersName = "sahil Jain",
            cifNumber = "TF-334",
            accountNumber = "PU23SDDLOJIJ",
            ifsc = "PKSSM09233",
            address = "Test Addre, Sssd, 1120"
        )
    }

    private fun setBankDetailsInfoOnView(
        name: String?,
        fathersName: String?,
        cifNumber: String?,
        accountNumber: String?,
        ifsc: String?,
        address: String?
    ) {
        nameTV.text = name
        fathersNameTV.text = fathersName
        cifNumberTV.text = cifNumber
        accNoTV.text = accountNumber
        ifscTV.text = ifsc
        addressTV.text = address
    }

}