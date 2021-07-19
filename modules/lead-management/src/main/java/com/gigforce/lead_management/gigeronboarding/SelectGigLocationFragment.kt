package com.gigforce.lead_management.gigeronboarding

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
import com.gigforce.lead_management.databinding.SelectGigLocationFragmentBinding

import java.util.regex.Pattern
import javax.inject.Inject

class SelectGigLocationFragment : BaseFragment2<SelectGigLocationFragmentBinding>(
    fragmentName = "SelectGigLocationFragment",
    layoutId = R.layout.select_gig_location_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = SelectGigLocationFragment()
        private const val TAG = "SelectGigLocationFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SelectGigLocationViewModel by viewModels()


    override fun viewCreated(
        viewBinding: SelectGigLocationFragmentBinding,
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