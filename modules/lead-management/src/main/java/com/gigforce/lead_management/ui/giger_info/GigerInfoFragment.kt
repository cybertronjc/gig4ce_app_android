package com.gigforce.lead_management.ui.giger_info

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.GigerInfoFragmentBinding
import com.gigforce.lead_management.ui.giger_onboarding.GigerOnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_below_giger_functionality.*
import javax.inject.Inject

@AndroidEntryPoint
class GigerInfoFragment : BaseFragment2<GigerInfoFragmentBinding>(
    fragmentName = "GigerInfoFragment",
    layoutId = R.layout.giger_info_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = GigerInfoFragment()
        private const val TAG = "GigerInfoFragment"
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: GigerInfoViewModel by viewModels()

    override fun viewCreated(viewBinding: GigerInfoFragmentBinding, savedInstanceState: Bundle?) {
        initToolbar(viewBinding)
        initListeners()
        initViewModel()
    }

    private fun initViewModel() {

    }

    private fun initListeners() = viewBinding.apply {
        bottomButtonLayout.dropGigerBtn.setOnClickListener {
            //drop functionality
        }
        bottomButtonLayout.callLayout.setOnClickListener {
            //call functionality
        }

        topLayout.backImageButton.setOnClickListener {
            //back functionality
        }
    }

    private fun initToolbar(viewBinding: GigerInfoFragmentBinding) = viewBinding.apply {

    }


}