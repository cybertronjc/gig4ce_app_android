package com.gigforce.app.modules.gigerVerfication.drivingLicense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.ImageSource
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheet
import com.gigforce.app.modules.gigerVerfication.SelectImageSourceBottomSheetActionListener
import kotlinx.android.synthetic.main.fragment_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_verification_image_holder.view.*
import java.io.File

enum class DrivingLicenseSides {
    FRONT_SIDE,
    BACK_SIDE
}

class AddDrivingLicenseInfoFragment : BaseFragment(), SelectImageSourceBottomSheetActionListener {

    private val viewModel: GigVerificationViewModel by viewModels()

    private var dlFrontImagePath: File? = null
    private var dlBackImagePath: File? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null

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

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        dlAvailaibilityOptionRG.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.dlYesRB) {
                showDLImageAndInfoLayout()
            } else {
                hideDLImageAndInfoLayout()
                enableSubmitButton()
            }
        }

        editDrivingLicenseInfoLayout.setOnClickListener {
            navigate(R.id.editDrivingLicenseInfoBottomSheet)
        }

        dlFrontImageHolder.uploadDocumentCardView.setOnClickListener {
            currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        dlBackImageHolder.uploadDocumentCardView.setOnClickListener {
            currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }
    }

    private fun showDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.VISIBLE
        dlFrontImageHolder.visibility = View.VISIBLE

    }

    private fun hideDLImageAndInfoLayout() {
        dlBackImageHolder.visibility = View.GONE
        dlFrontImageHolder.visibility = View.GONE
        dlInfoLayout.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        dlSubmitSliderBtn.isEnabled = true
    }

    private fun showImageInfoLayout() {
        dlInfoLayout.visibility = View.VISIBLE
    }

    override fun onImageSourceSelected(source: ImageSource) {
        showImageInfoLayout()

        if (currentlyClickingImageOfSide == null)
            return
        else if (currentlyClickingImageOfSide == DrivingLicenseSides.BACK_SIDE) {
            dlBackImagePath = File("ma")
            enableSubmitButton()
        } else if (currentlyClickingImageOfSide == DrivingLicenseSides.FRONT_SIDE) {
            dlFrontImagePath = File("ma")
            enableSubmitButton()
        }

        setDLInfoOnView(
            name = "Rahul Jr",
            dob = "11/09/1990",
            fathersName = "Male",
            licenseNo = "2345 7624 9238",
            licenseValidity = "10/2030",
            address = "PU23SDDLOJIJ"
        )
    }

    private fun setDLInfoOnView(
        name: String?,
        fathersName: String?,
        dob: String?,
        licenseNo: String?,
        licenseValidity: String?,
        address: String?
    ) {
        nameTV.text = name
        fathersNameTV.text = fathersName
        dobTV.text = dob
        licenseNoTV.text = licenseNo
        licenseValidityTV.text = licenseValidity
        addOnlicenseTV.text = address
    }

}