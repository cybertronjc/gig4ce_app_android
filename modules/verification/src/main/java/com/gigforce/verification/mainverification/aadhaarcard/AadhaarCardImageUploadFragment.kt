package com.gigforce.verification.mainverification.aadhaarcard

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardImageUploadFragmentBinding
import com.gigforce.verification.gigerVerfication.aadharCard.AadharCardSides
import com.gigforce.verification.gigerVerfication.aadharCard.AddAadharCardInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import javax.inject.Inject

@AndroidEntryPoint
class AadhaarCardImageUploadFragment : Fragment() {

    companion object {
        fun newInstance() = AadhaarCardImageUploadFragment()
        const val REQUEST_CODE_UPLOAD_AADHAR_IMAGE = 2333
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: AadhaarCardImageUploadViewModel by viewModels()
    private lateinit var viewBinding : AadhaarCardImageUploadFragmentBinding
    private var aadharFrontImagePath: Uri? = null
    private var aadharBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: AadharCardSides? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadhaarCardImageUploadFragmentBinding.inflate(inflater,container,false)
        return viewBinding.root
//        return inflater.inflate(R.layout.aadhaar_card_image_upload_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        observer()
        listeners()

    }

    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            if (viewBinding.toplayoutblock.viewPager2.currentItem == 0) openCameraAndGalleryOptionForFrontSideImage() else openCameraAndGalleryOptionForBackSideImage()
        })
    }

    private fun observer() {

    }

    private fun setViews() {
        val frontUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_front))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_front))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_front))
            .build()
        val backUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_back))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_back))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_back))
            .build()
        val list = listOf(KYCImageModel(getString(R.string.upload_aadhar_card_front_side_new), frontUri, false), KYCImageModel(getString(R.string.upload_aadhar_card_back_side_new), backUri, false))
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.FRONT_SIDE

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
            "purpose",
            "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "aadhar_card_front.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,
            REQUEST_CODE_UPLOAD_AADHAR_IMAGE, requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)

    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = AadharCardSides.BACK_SIDE

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
            "purpose",
            "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "aadhar_card_back.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,
            REQUEST_CODE_UPLOAD_AADHAR_IMAGE, requireContext(),this)
//        startActivityForResult(photoCropIntent, REQUEST_CODE_UPLOAD_AADHAR_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddAadharCardInfoFragment.REQUEST_CODE_UPLOAD_AADHAR_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {

                if (AadharCardSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    aadharFrontImagePath =
                        data?.getParcelableExtra("uri")
                    showFrontAadharCard(aadharFrontImagePath!!)
                } else if (AadharCardSides.BACK_SIDE == currentlyClickingImageOfSide) {
                    aadharBackImagePath =
                        data?.getParcelableExtra("uri")
                    showBackAadharCard(aadharBackImagePath!!)
                }

//                if (aadharDataCorrectCB.isChecked
//                    && aadharFrontImagePath != null
//                    && aadharBackImagePath != null
//                ) {
//                    enableSubmitButton()
//                } else {
//                    disableSubmitButton()
//                }
//
//                if (aadharFrontImagePath != null && aadharBackImagePath != null && aadharSubmitSliderBtn.isGone) {
//                    aadharSubmitSliderBtn.visible()
//                    aadharDataCorrectCB.visible()
//                }

            }
        }
    }
//    private fun showAadharImageAndInfoLayout() {
//        aadharBackImageHolder.visibility = View.VISIBLE
//        aadharFrontImageHolder.visibility = View.VISIBLE
//    }
//
//    private fun hideAadharImageAndInfoLayout() {
//        aadharBackImageHolder.visibility = View.GONE
//        aadharFrontImageHolder.visibility = View.GONE
//        aadharInfoLayout.visibility = View.GONE
//    }

//    private fun enableSubmitButton() {
//        aadharSubmitSliderBtn.isEnabled = true
//
//        aadharSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        aadharSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }
//
//    private fun disableSubmitButton() {
//        aadharSubmitSliderBtn.isEnabled = false
//
//        aadharSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        aadharSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }
//
//    private fun showImageInfoLayout() {
//        aadharInfoLayout.visibility = View.VISIBLE
//    }
//
//
    private fun showFrontAadharCard(aadharFrontImagePath: Uri) {
//        aadharFrontImageHolder.makeEditLayoutVisible()
//        aadharFrontImageHolder.uploadImageLabel(getString(R.string.aadhar_card_front_image))
//
//        aadharFrontImageHolder.setImage(aadharFrontImagePath)
        viewBinding.toplayoutblock.setDocumentImage(0, aadharFrontImagePath)

    }

    private fun showBackAadharCard(aadharBackImagePath: Uri) {
//        aadharBackImageHolder.makeUploadLayoutVisible()
//        aadharBackImageHolder.uploadImageLabel(getString(R.string.aadhar_card_back_image))
//
//        aadharBackImageHolder .setImage(aadharBackImagePath)
        viewBinding.toplayoutblock.setDocumentImage(1, aadharBackImagePath)
    }
//



}