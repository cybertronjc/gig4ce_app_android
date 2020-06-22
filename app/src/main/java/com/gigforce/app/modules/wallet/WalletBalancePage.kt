package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import kotlinx.android.synthetic.main.payment_summary_component.view.*
import kotlinx.android.synthetic.main.wallet_balance_page.*
import kotlinx.android.synthetic.main.wallet_top_bar_component.*

class WalletBalancePage: WalletBaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.wallet_balance_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        payment_summary.invoice_due.setOnClickListener {
            navigate(R.id.invoiceStatusPage)
        }

        payment_summary.payment_dispute.setOnClickListener {
            navigate(R.id.paymentDisputePage)
        }

        payment_summary.monthly_earning.setOnClickListener {
            navigate(R.id.monthlyEarningPage)
        }

        back_button.setOnClickListener{
            activity?.onBackPressed()
        }

        balance_card.setOnClickListener { navigate(R.id.walletExpandedPage) }

        walletViewModel.userProfileData.observe(viewLifecycleOwner, Observer {
            top_bar.imageName = it.profileAvatarName
        })
    }

    private fun initialize() {
        walletViewModel.userWallet.observe(viewLifecycleOwner, Observer {userWallet ->
            userWallet?.let { wallet ->
                zero_balance.visibility = if (wallet.balance == 0) View.VISIBLE else View.GONE
                non_zero_balance.visibility = if (wallet.balance == 0) View.GONE else View.VISIBLE

                monthly_goal_card.isMonthlyGoalSet = wallet.isMonthlyGoalSet
                balance_card.balance = wallet.balance

                payment_summary.monthlyEarning = wallet.monthlyEarnedAmount
                payment_summary.invoiceAmount = 4000
                payment_summary.paymentDueAmount = 0

                var t0 = invoiceViewModel.generatedInvoice.value?.get(0)
                var t1 = invoiceViewModel.generatedInvoice.value?.get(1)
                var t2 = invoiceViewModel.generatedInvoice.value?.get(2)

                t0?.let {
                    transaction_1.agent = it.agentName
                    transaction_1.amount = it.gigAmount
                    transaction_1.status = it.invoiceStatus
                    transaction_1.timings = it.gigTiming
                }
                t1?.let {
                    transaction_2.agent = it.agentName
                    transaction_2.amount = it.gigAmount
                    transaction_2.status = it.invoiceStatus
                    transaction_2.timings = it.gigTiming
                }
                t2?.let {
                    transaction_3.agent = it.agentName
                    transaction_3.amount = it.gigAmount
                    transaction_3.status = it.invoiceStatus
                    transaction_3.timings = it.gigTiming
                }

                monthly_goal_card.currentMonthSalary = wallet.monthlyEarnedAmount
                monthly_goal_card.monthlyGoalAmount = wallet.monthlyGoalLimit

                Log.d("WBP", monthly_goal_card.monthlyGoalAmount.toString())

                monthly_goal_card.isMonthlyGoalSet = wallet.isMonthlyGoalSet
            }

        })
    }
}