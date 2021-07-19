package com.gigforce.lead_management.ui.select_shift_timing

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ShiftTimingFragmentBinding
import javax.inject.Inject

class ShiftTimingFragment : BaseFragment2<ShiftTimingFragmentBinding>(
    fragmentName = "ShiftTimingFragment",
    layoutId = R.layout.select_gig_application_to_activate_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = ShiftTimingFragment()
        private const val TAG = "ShiftTimingFragment"
        private var isNumberRegistered = false
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: ShiftTimingViewModel by viewModels()


    override fun viewCreated(
        viewBinding: ShiftTimingFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initListeners()
        initViewModel()
    }

    private fun initViewModel() {

    }

    private fun initListeners() {

    }


}