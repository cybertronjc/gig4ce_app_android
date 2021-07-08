package com.gigforce.verification.mainverification.aadhaarcard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardPhoneNumberFragmentBinding
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*

class AadhaarCardPhoneNumberFragment : Fragment() {

    companion object {
        fun newInstance() = AadhaarCardPhoneNumberFragment()
    }

    private val viewModel: AadhaarCardPhoneNumberViewModel by viewModels()
    private lateinit var viewBinding : AadhaarCardPhoneNumberFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadhaarCardPhoneNumberFragmentBinding.inflate(inflater,container,false)
        return viewBinding.root
//        return inflater.inflate(R.layout.aadhaar_card_phone_number_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {
        viewBinding.toplayoutblock.querytext.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.imageView7.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this),
            content = getString(R.string.why_we_need_this_aadhar)
        )
    }

}