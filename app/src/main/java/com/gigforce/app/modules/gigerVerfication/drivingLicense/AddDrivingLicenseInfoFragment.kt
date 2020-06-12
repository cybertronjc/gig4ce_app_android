package com.gigforce.app.modules.gigerVerfication.drivingLicense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import kotlinx.android.synthetic.main.fragment_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*

class AddDrivingLicenseInfoFragment : BaseFragment() {

    private val viewModel: GigVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_driving_license_info, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        dlFrontImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_driving_license_front_side)
        dlFrontImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_driving_license)

        dlBackImageHolder.documentUploadLabelTV.text =
            getString(R.string.upload_driving_license_back_side)
        dlBackImageHolder.documentUploadSubLabelTV.text =
            getString(R.string.upload_your_driving_license)
        dlSubmitSliderBtn.isEnabled = false

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()
            } else {
                hideDLImageAndInfoLayout()
                enableSubmitButton()
            }
        }
    }

    private fun showDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.VISIBLE
        dlFrontImageHolder.visibility = View.VISIBLE
        dlInfoLayout.visibility = View.VISIBLE
    }

    private fun hideDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.GONE
        dlFrontImageHolder.visibility = View.GONE
        dlInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        dlSubmitSliderBtn.isEnabled = true
    }

}