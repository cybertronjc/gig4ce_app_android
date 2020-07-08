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
import com.gigforce.app.modules.wallet.components.InvoiceCollapsedCard
import com.gigforce.app.modules.wallet.models.Invoice
import kotlinx.android.synthetic.main.help_expanded_page.*
import kotlinx.android.synthetic.main.payment_summary_component.view.*
import kotlinx.android.synthetic.main.wallet_balance_page.*
import kotlinx.android.synthetic.main.wallet_balance_page.top_bar
import kotlinx.android.synthetic.main.wallet_top_bar_component.*
import kotlinx.android.synthetic.main.wallet_top_bar_component.back_button

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

        setListeners()
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

        transaction_head.setOnClickListener {navigate(R.id.walletExpandedPage)}

        walletViewModel.userProfileData.observe(viewLifecycleOwner, Observer {
            top_bar.imageName = it.profileAvatarName
        })
    }

    private fun initialize() {
        walletViewModel.userWallet.observe(viewLifecycleOwner, Observer {userWallet ->
            userWallet?.let { wallet ->
                zero_balance.visibility = if (wallet.balance == 0F) View.VISIBLE else View.GONE
                non_zero_balance.visibility = if (wallet.balance == 0F) View.GONE else View.VISIBLE

                monthly_goal_card.isMonthlyGoalSet = wallet.isMonthlyGoalSet
                balance_card.balance = wallet.balance

                payment_summary.monthlyEarning = wallet.monthlyEarnedAmount
                payment_summary.invoiceAmount = 4000

                monthly_goal_card.currentMonthSalary = wallet.monthlyEarnedAmount
                monthly_goal_card.monthlyGoalAmount = wallet.monthlyGoalLimit

                Log.d("WBP", monthly_goal_card.monthlyGoalAmount.toString())

                monthly_goal_card.isMonthlyGoalSet = wallet.isMonthlyGoalSet
            }

        })

        invoiceViewModel.allInvoices.observe(viewLifecycleOwner, Observer {
            payment_summary.paymentDueAmount = invoiceViewModel.getPaymentDueAmount(it)

            // TODO: replace by function
            val topTransactions = invoiceViewModel.allInvoices.value!!

            insertTopTransactions(topTransactions)
        })
    }

    private fun setListeners() {

    }

    private fun insertTopTransactions(tnx: ArrayList<Invoice>) {
        if (tnx.size == 0) {
            zero_balance.visibility = View.VISIBLE
            non_zero_balance.visibility = View.GONE
        } else {
            zero_balance.visibility = View.GONE
            non_zero_balance.visibility = View.VISIBLE
        }
        for (transaction in tnx) {
            val widget = InvoiceCollapsedCard(requireContext())
            transactions.addView(widget)
        }
    }
}