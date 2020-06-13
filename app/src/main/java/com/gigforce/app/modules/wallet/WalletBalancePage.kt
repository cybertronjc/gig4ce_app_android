package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import kotlinx.android.synthetic.main.payment_summary_component.view.*
import kotlinx.android.synthetic.main.wallet_balance_page.*

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
    }

    private fun initialize() {
        walletViewModel.userWallet.observe(viewLifecycleOwner, Observer {
            it.let {
                zero_balance.visibility = if (it.balance == 0) View.VISIBLE else View.GONE
                non_zero_balance.visibility = if (it.balance == 0) View.GONE else View.VISIBLE

                monthly_goal_card.isMonthlyGoalSet = false
            }
        })
    }
}