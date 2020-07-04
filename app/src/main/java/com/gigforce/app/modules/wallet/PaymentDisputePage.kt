package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.modules.wallet.adapters.InvoiceAdapter
import com.gigforce.app.modules.wallet.models.Invoice
import com.gigforce.app.modules.wallet.models.TransactionAdapter
import com.jay.widget.StickyHeadersLinearLayoutManager
import kotlinx.android.synthetic.main.balance_expanded_page.*
import kotlinx.android.synthetic.main.fragment_select_language.*
import kotlinx.android.synthetic.main.payment_dispute_page.*
import kotlinx.android.synthetic.main.payment_dispute_page.back_button

class PaymentDisputePage: WalletBaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.payment_dispute_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setListeners()

    }

    private fun initialize() {
        invoiceViewModel.allInvoices.observe(viewLifecycleOwner, Observer {allInvoices ->
            invoiceViewModel.getDisputedInvoices(allInvoices)?.let {
                if (it.size > 0) no_dispute.visibility = View.GONE
                else no_dispute.visibility = View.VISIBLE

                disputedInvoices.apply {
                    layoutManager = StickyHeadersLinearLayoutManager<InvoiceAdapter>(requireContext())
                    adapter = InvoiceAdapter(ArrayList(InvoiceAdapter.arrangeTransactions(it)))
                }
            }
        })
    }

    private fun setListeners() {
        back_button.setOnClickListener { requireActivity().onBackPressed() }
        help_ic.setOnClickListener { navigate(R.id.helpExpandedPage) }
    }
}