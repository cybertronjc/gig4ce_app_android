package com.gigforce.app.tl_work_space.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentTlWorkspaceHomeBinding
import com.gigforce.core.base.BaseFragment2
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TLWorkspaceHomeFragment : BaseFragment2<FragmentTlWorkspaceHomeBinding>(
    fragmentName = "TLWorkspaceHomeFragment",
    layoutId = R.layout.fragment_tl_workspace_home,
    statusBarColor = R.color.status_bar_pink
) {
    private val viewModel: TLWorkspaceHomeViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentTlWorkspaceHomeBinding,
        savedInstanceState: Bundle?
    ) {

        if(viewCreatedForTheFirstTime){

        }
    }

}