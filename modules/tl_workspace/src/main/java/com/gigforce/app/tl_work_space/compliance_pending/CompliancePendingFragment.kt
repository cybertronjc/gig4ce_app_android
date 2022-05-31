package com.gigforce.app.tl_work_space.compliance_pending

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentCompliancePendingBinding
import com.gigforce.core.base.BaseFragment2

class CompliancePendingFragment : BaseFragment2<FragmentCompliancePendingBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_compliance_pending,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "CompliancePendingFragment"
    }
    private val viewModel: CompliancePendingViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentCompliancePendingBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {

            initView()
            initViewModel()
        }
    }

    private fun initView() {
        TODO("Not yet implemented")
    }

    private fun initViewModel() {
        TODO("Not yet implemented")
    }
}