package com.gigforce.verification.mainverification.drivinglicense

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.DrivingLicenseFragmentBinding
import com.gigforce.verification.gigerVerfication.drivingLicense.AddDrivingLicenseInfoFragment
import com.gigforce.verification.gigerVerfication.drivingLicense.DrivingLicenseSides
import com.gigforce.verification.gigerVerfication.panCard.AddPanCardInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import javax.inject.Inject

@AndroidEntryPoint
class DrivingLicenseFragment : Fragment() {
    companion object {
        fun newInstance() = DrivingLicenseFragment()
        const val REQUEST_CODE_UPLOAD_DL = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_FRONT = "front_image"
        const val INTENT_EXTRA_CLICKED_IMAGE_BACK = "back_image"
        const val INTENT_EXTRA_STATE = "state"
        const val INTENT_EXTRA_DL_NO = "dl_no"
    }

    @Inject
    lateinit var navigation: INavigation

    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: DrivingLicenseViewModel by viewModels()
    private lateinit var viewBinding: DrivingLicenseFragmentBinding
    private var dlFrontImagePath: Uri? = null
    private var dlBackImagePath: Uri? = null
    private var currentlyClickingImageOfSide: DrivingLicenseSides? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewBinding = DrivingLicenseFragmentBinding.inflate(inflater,container,false)
        return viewBinding.root
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
        val list = listOf(KYCImageModel(getString(R.string.upload_driving_license_front_side_new), frontUri, false), KYCImageModel(getString(R.string.upload_driving_license_back_side_new), backUri, false))
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun openCameraAndGalleryOptionForFrontSideImage() {
        currentlyClickingImageOfSide = DrivingLicenseSides.FRONT_SIDE
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
            REQUEST_CODE_UPLOAD_DL,requireContext(),this)


    }

    private fun openCameraAndGalleryOptionForBackSideImage() {
        currentlyClickingImageOfSide = DrivingLicenseSides.BACK_SIDE
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
            REQUEST_CODE_UPLOAD_DL,requireContext(),this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {

                if (DrivingLicenseSides.FRONT_SIDE == currentlyClickingImageOfSide) {
                    dlFrontImagePath =
                        data?.getParcelableExtra("uri")
                    showFrontDrivingLicense(dlFrontImagePath!!)
                } else if (DrivingLicenseSides.BACK_SIDE == currentlyClickingImageOfSide) {
                    dlBackImagePath =
                        data?.getParcelableExtra("uri")
                    showBackDrivingLicense(dlBackImagePath!!)
                }

//                if (confirmDLDataCB_client_act.isChecked
//                    && dlFrontImagePath != null
//                    && dlBackImagePath != null
//                ) {
//                    enableSubmitButton()
//                }
//
//                if (dlFrontImagePath != null && dlBackImagePath != null && dlSubmitSliderBtn_client_act.isGone) {
//                    dlSubmitSliderBtn_client_act.visible()
//                    confirmDLDataCB_client_act.visible()
//                }
            }
        }
    }


//    private fun showDLImageAndInfoLayout() {
//        dlBackImageHolder.visibility = View.VISIBLE
//        dlFrontImageHolder.visibility = View.VISIBLE
//        showImageInfoLayout()
//    }
//
//    private fun hideDLImageAndInfoLayout() {
//        dlBackImageHolder.visibility = View.GONE
//        dlFrontImageHolder.visibility = View.GONE
//        dlInfoLayout.visibility = View.GONE
//    }
//
//    private fun enableSubmitButton() {
//        dlSubmitSliderBtn_client_act.isEnabled = true
//
//        dlSubmitSliderBtn_client_act.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        dlSubmitSliderBtn_client_act.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }
//
//    private fun disableSubmitButton() {
//        dlSubmitSliderBtn_client_act.isEnabled = false
//
//        dlSubmitSliderBtn_client_act.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        dlSubmitSliderBtn_client_act.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }
//
//
//    private fun showImageInfoLayout() {
//        dlInfoLayout.visibility = View.VISIBLE
//    }


    private fun showFrontDrivingLicense(drivingFrontPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(0, drivingFrontPath)
    }

    private fun showBackDrivingLicense(drivingBackPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(1, drivingBackPath)
    }

}