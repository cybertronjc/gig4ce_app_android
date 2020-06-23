package com.gigforce.app.modules.gigerVerfication.drivingLicense

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
            } else if(checkedId == R.id.dlNoRB && confirmDLDataCB.isChecked){
                hideDLImageAndInfoLayout()
                enableSubmitButton()
            } else
                disableSubmitButton()
        }

        confirmDLDataCB.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {

                if (dlYesRB.isChecked && dlFrontImagePath != null)
                    enableSubmitButton()
                else if (dlNoRB.isChecked)
                    enableSubmitButton()
                else
                    disableSubmitButton()
            } else
                disableSubmitButton()
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

        dlFrontImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_driving_license_front_side)

        dlBackImageHolder.uploadImageLayout.imageLabelTV.text =
            getString(R.string.upload_driving_license_back_side)

        dlFrontImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
            currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE

            SelectImageSourceBottomSheet.launch(
                childFragmentManager = childFragmentManager,
                selectImageSourceBottomSheetActionListener = this
            )
        }

        dlBackImageHolder.uploadImageLayout.reuploadBtn.setOnClickListener {
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

        dlSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        dlSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    private fun disableSubmitButton() {
        dlSubmitSliderBtn.isEnabled = false

        dlSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        dlSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
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

            if (confirmDLDataCB.isChecked)
                enableSubmitButton()

            showBackDrivingLicense()
        } else if (currentlyClickingImageOfSide == DrivingLicenseSides.FRONT_SIDE) {
            dlFrontImagePath = File("ma")

            if (confirmDLDataCB.isChecked)
                enableSubmitButton()

            showFrontDrivingLicense()
        }

        setDLInfoOnView(
            name = "Rahul Jain",
            dob = "11/09/1990",
            fathersName = "Male",
            licenseNo = "DL234576249238",
            licenseValidity = "10/2030",
            address = "House no 3432, Preet Vihar, New Delhi, Delhi 112034"
        )
    }

    private fun showFrontDrivingLicense(aadharFrontImagePath: File? = null) {
        dlFrontImageHolder.uploadDocumentCardView.visibility = View.GONE
        dlFrontImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(R.drawable.bg_dl)
            .into(dlFrontImageHolder.uploadImageLayout.clickedImageIV)
    }

    private fun showBackDrivingLicense(aadharBackImagePath: File? = null) {
        dlBackImageHolder.uploadDocumentCardView.visibility = View.GONE
        dlBackImageHolder.uploadImageLayout.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(R.drawable.bg_dl)
            .into(dlBackImageHolder.uploadImageLayout.clickedImageIV)
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