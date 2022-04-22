package com.gigforce.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.gigforce.wallet.adapters.MonthlyPayslipsAdapter
import com.gigforce.wallet.models.Payslip
import com.gigforce.wallet.vm.PayslipMonthlyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_payslip_monthly.*
import kotlinx.android.synthetic.main.fragment_payslip_monthly_main.*
import kotlinx.android.synthetic.main.wallet_balance_card_component.view.*
import kotlinx.android.synthetic.main.wallet_top_bar_component.view.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PayslipMonthlyFragment : Fragment() {

    private val viewModel: PayslipMonthlyViewModel by activityViewModels()

    @Inject
    lateinit var buildConfig: IBuildConfig
    private val mAdapter: MonthlyPayslipsAdapter by lazy {
        MonthlyPayslipsAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_payslip_monthly, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData(savedInstanceState)
        initView()
        initViewModel()
    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }

    private fun initView() {

        payslip_recycler_view.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        mAdapter.setOnPaySlipClickActionListener {
            viewModel.downloadPaySlip(it, requireContext().filesDir)
        }

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

        viewModel.downloadPaySlip
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> {
                        showDownloadingDialog()
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        openPdf(it.content)
                    }
                    is Lce.Error -> {
                        UtilMethods.hideLoading()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_wallet))
                            .setMessage(it.error)
                            .setPositiveButton(getString(R.string.okay_wallet)) { _, _ -> }
                            .show()
                    }
                }
            })

        viewModel.getPaySlips()
    }

    private fun showDownloadingDialog() {
        UtilMethods.showLoading(requireContext())
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



        mAdapter.updateCourseContent(content.sortedByDescending { it.getDateOfPaymentLocalDate() })
    }

    private fun showErrorInLoadingPayslips(error: String) {

        payslip_monthly_main_layout.gone()
        payslip_monthly_progress_bar.gone()

        payslip_monthly_details_error.visible()
        payslip_monthly_details_error.text = error
    }

    private fun openPdf(file: File) {

        if (file.exists()) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    FileProvider.getUriForFile(
                        requireContext(),
                        buildConfig.getApplicationID() + ".provider",
                        file
                    ), "application/pdf"
                )
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(this)
                } catch (e: Exception) {
                    showErrorDialog(getString(R.string.unable_to_open_wallet))
                }
            }
        } else {
            showErrorDialog(getString(R.string.file_doesnt_exist_wallet))
        }
    }

    private fun showErrorDialog(error: String) {

    }
}