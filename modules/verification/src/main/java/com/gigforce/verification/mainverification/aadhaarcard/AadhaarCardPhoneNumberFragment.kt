package com.gigforce.verification.mainverification.aadhaarcard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardPhoneNumberFragmentBinding
import com.gigforce.verification.mainverification.WhyWeNeedThisBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import javax.inject.Inject

@AndroidEntryPoint
class AadhaarCardPhoneNumberFragment : Fragment() {

    companion object {
        fun newInstance() = AadhaarCardPhoneNumberFragment()
    }
    @Inject
    lateinit var navigation: INavigation

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
        viewBinding.toplayoutblock.whyweneedit.setOnClickListener {
            showWhyWeNeedThisDialog()
        }
        viewBinding.toplayoutblock.iconwhyweneed.setOnClickListener {
            showWhyWeNeedThisDialog()
        }

        viewBinding.appBar2.setBackButtonListener(View.OnClickListener {
            navigation.popBackStack()
        })
    }

    private fun showWhyWeNeedThisDialog() {
        WhyWeNeedThisBottomSheet.launch(
            childFragmentManager = childFragmentManager,
            title = getString(R.string.why_do_we_need_this_veri),
            content = getString(R.string.why_we_need_this_aadhar_veri)
        )
    }

}