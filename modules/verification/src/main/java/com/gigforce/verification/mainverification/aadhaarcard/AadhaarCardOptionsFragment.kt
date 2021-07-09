package com.gigforce.verification.mainverification.aadhaarcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardOptionsFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.aadhaar_card_options_fragment.*
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import javax.inject.Inject


@AndroidEntryPoint
class AadhaarCardOptionsFragment : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() =
            AadhaarCardOptionsFragment()
    }

    private val viewModel: AadhaarCardOptionsViewModel by viewModels()
    private lateinit var viewBinding: AadhaarCardOptionsFragmentBinding

    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadhaarCardOptionsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {
        viewBinding.uploadAdhaarCard.setOnclickListner(this)
        viewBinding.phoneNumberAadhaar.setOnclickListner(this)
        viewBinding.next.setOnClickListener {
            if (viewBinding.uploadAdhaarCard.getIsSelected()) navigation.navigateTo("verification/aadhaarcardimageupload")
            else navigation.navigateTo("verification/aadhaarcardphonenumber")
        }

       viewBinding.textView5.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.imageView7.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        appBarAadharOption.apply {
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
    }

    override fun onClick(view: View?) {
        viewBinding.uploadAdhaarCard.setViewSelected(false)
        viewBinding.phoneNumberAadhaar.setViewSelected(false)
//        if(view?.id == viewBinding.uploadAdhaarCard.id){
//
//            viewBinding.uploadAdhaarCard.setViewSelected(true)
//
//        }
//        else{
//            viewBinding.phoneNumberAadhaar.setViewSelected(true)
//        }

    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this),
            content = getString(R.string.why_do_we_need_this_aadhar)
        )
    }
}