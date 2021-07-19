package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigApplicationToActivateFragmentBinding
import javax.inject.Inject

class SelectGigApplicationToActivate : BaseFragment2<SelectGigApplicationToActivateFragmentBinding>(
    fragmentName = "SelectGigApplicationFragment",
    layoutId = R.layout.select_gig_application_to_activate_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = SelectGigApplicationToActivate()
        private const val TAG = "SelectGigApplicationFragment"
        private var isNumberRegistered = false
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SelectGigApplicationToActivateViewModel by viewModels()


    override fun viewCreated(
        viewBinding: SelectGigApplicationToActivateFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initViews()
        initListeners()
        initViewModel()
    }

    private fun initViews() {
        viewBinding.gigerProfileCard.apply {
            setProfileCard(GigerProfileCardDVM("https://instagram.fdel11-2.fna.fbcdn.net/v/t51.2885-19/s320x320/125221466_394003705121691_8790543636526463384_n.jpg", "Jagdish Choudhary", "+919898833257", "Swiggy delivery", ""))
        }
    }

    private fun initViewModel() {

    }

    private fun initListeners() {

    }

}