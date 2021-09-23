package com.gigforce.lead_management.ui.new_selection_form_submittion_success

import android.os.Bundle
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionFormSuccessBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectionFormSubmitSuccessFragment : BaseFragment2<FragmentNewSelectionFormSuccessBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_new_selection_form_success,
    statusBarColor = R.color.lipstick_2
){

    companion object{
        private const val TAG = "SelectionFormSubmitSuccessFragment"
        const val INTENT_EXTRA_SHARE_LINK = "share_link"
    }

    @Inject
    lateinit var navigation : INavigation

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionFormSuccessBinding,
        savedInstanceState: Bundle?
    ) {
        initView()
        initListener()
    }

    private fun initView() = viewBinding.toolbar.apply{
        setBackButtonListener {
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }
    }

    private fun initListener() = viewBinding.apply {

        nextButton.setOnClickListener {
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }
    }
}