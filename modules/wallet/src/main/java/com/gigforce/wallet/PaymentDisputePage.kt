package com.gigforce.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.gigforce.wallet.adapters.InvoiceAdapter
import com.gigforce.core.navigation.INavigation
import com.jay.widget.StickyHeadersLinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.payment_dispute_page.*
import kotlinx.android.synthetic.main.payment_dispute_page.back_button
import javax.inject.Inject

@AndroidEntryPoint
class PaymentDisputePage: WalletBaseFragment() {
    @Inject lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.payment_dispute_page, container, false)
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
        help_ic.setOnClickListener { navigation.navigateTo("wallet/helpExpandedPage") }
    }
}