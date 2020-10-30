package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.wallet.adapters.MonthlyPayslipsAdapter
import com.gigforce.app.modules.wallet.models.Payslip
import com.gigforce.app.utils.Lce
import kotlinx.android.synthetic.main.fragment_payslip_monthly.*
import kotlinx.android.synthetic.main.fragment_payslip_monthly_main.*
import kotlinx.android.synthetic.main.fragment_payslip_monthly_main.top_bar
import kotlinx.android.synthetic.main.wallet_balance_page.*
import kotlinx.android.synthetic.main.wallet_top_bar_component.view.*

class PayslipMonthlyFragment : BaseFragment() {

    private val viewModel: PayslipMonthlyViewModel by viewModels()

    private val mAdapter: MonthlyPayslipsAdapter by lazy {
        MonthlyPayslipsAdapter(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_payslip_monthly, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() {

        top_bar.back_button.setOnClickListener {
            activity?.onBackPressed()
        }

        payslip_recycler_view.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
        )
        payslip_recycler_view.adapter = mAdapter
    }

    private fun initViewModel() {

        viewModel
                .monthlySlips
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> showPayslipsLoadingLayout()
                        is Lce.Content -> showPayslipsOnView(it.content)
                        is Lce.Error -> showErrorInLoadingPayslips(it.error)
                    }
                })

        viewModel
            .userProfileData
            .observe(viewLifecycleOwner, Observer {
                top_bar.imageName = it.profileAvatarName
            })

        viewModel.getPaySlips()
    }

    private fun showPayslipsLoadingLayout() {

        payslip_monthly_main_layout.gone()
        payslip_monthly_details_error.gone()
        payslip_monthly_progress_bar.visible()
    }

    private fun showPayslipsOnView(content: List<Payslip>) {

        payslip_monthly_details_error.gone()
        payslip_monthly_progress_bar.gone()
        payslip_monthly_main_layout.visible()

        mAdapter.updateCourseContent(content)
    }

    private fun showErrorInLoadingPayslips(error: String) {

        payslip_monthly_main_layout.gone()
        payslip_monthly_progress_bar.gone()

        payslip_monthly_details_error.visible()
        payslip_monthly_details_error.text = error
    }
}