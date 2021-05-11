package com.gigforce.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.wallet.adapters.MonthlyPayslipsAdapter
import com.gigforce.wallet.models.Payslip
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
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
    @Inject lateinit var buildConfig : IBuildConfig
    private val mAdapter: MonthlyPayslipsAdapter by lazy {
        MonthlyPayslipsAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_payslip_monthly, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() {

        top_bar.search_icon.gone()
        //top_bar.wallet_heading.text = "My Payslips"

        balance_card.renew_icon.gone()
        balance_card.last_updated_text.gone()

        top_bar.back_button.setOnClickListener {
            activity?.onBackPressed()
        }

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
                            .setTitle("Alert")
                            .setMessage(it.error)
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                }
            })

        viewModel
            .userProfileData
            .observe(viewLifecycleOwner, Observer {
                top_bar.imageName = it.profileAvatarName
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
        payslip_label.isVisible = content.isNotEmpty()

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
                    showErrorDialog("Unable to open")
                }
            }
        } else {
            showErrorDialog("file_doesnt_exist")
        }
    }

    private fun showErrorDialog(error: String) {

    }
}