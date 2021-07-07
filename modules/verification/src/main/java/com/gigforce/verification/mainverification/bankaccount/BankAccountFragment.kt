package com.gigforce.verification.mainverification.bankaccount

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
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.BankAccountFragmentBinding
import com.gigforce.verification.databinding.PanCardFragmentBinding
import com.gigforce.verification.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BankAccountFragment : Fragment() {

    companion object {
        fun newInstance() = BankAccountFragment()
        const val REQUEST_CODE_CAPTURE_BANK_PHOTO = 2333
        const val INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT = "user_came_from_amb_screen"
    }

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewModel: BankAccountViewModel
    private var didUserCameFromAmbassadorScreen = false
    private var clickedImagePath: Uri? = null
    private lateinit var viewBinding: BankAccountFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = BankAccountFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        observer()
        listeners()
    }
    private fun observer() {

    }
    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            showCameraAndGalleryOption()
        })
    }

    private fun setViews() {
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_front))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_front))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_front))
            .build()
        val list = listOf(KYCImageModel(getString(R.string.upload_bank_account_new), frontUri, false))
        viewBinding.toplayoutblock.setImageViewPager(list)
    }


    private fun showCameraAndGalleryOption() {
        val photoCropIntent = Intent()
        photoCropIntent.putExtra(
            "purpose",
            "verification"
        )
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "pan_card.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,
            REQUEST_CODE_CAPTURE_BANK_PHOTO,requireContext(),this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddBankDetailsInfoFragment.REQUEST_CODE_CAPTURE_BANK_PHOTO) {

            if (resultCode == Activity.RESULT_OK) {
                clickedImagePath =
                    data?.getParcelableExtra("uri")
                showPassbookInfoCard(clickedImagePath!!)

//                if (bankDetailsDataConfirmationCB.isChecked)
//                    enableSubmitButton()
//
//                if (clickedImagePath != null && passbookSubmitSliderBtn.isGone) {
//                    bankDetailsDataConfirmationCB.visible()
//                    passbookSubmitSliderBtn.visible()
//                }

            }
        }
    }

//    private fun disableSubmitButton() {
//        passbookSubmitSliderBtn.isEnabled = false
//
//        passbookSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        passbookSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }
//
//    private fun showPassbookImageLayout() {
//        passbookImageHolder.visibility = View.VISIBLE
//    }
//
//    private fun showPassbookInfoLayout() {
//        passbookInfoLayout.visibility = View.VISIBLE
//    }
//
//    private fun hidePassbookImageAndInfoLayout() {
//        passbookImageHolder.visibility = View.GONE
//        passbookInfoLayout.visibility = View.GONE
//    }
//
//    private fun enableSubmitButton() {
//        passbookSubmitSliderBtn.isEnabled = true
//
//        passbookSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        passbookSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }


    private fun showPassbookInfoCard(bankInfoPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(0, bankInfoPath)
    }


}