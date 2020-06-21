package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_select_language.*
import kotlinx.android.synthetic.main.payment_dispute_page.*

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
        payment_1.invoiceStatus = "rejected"
        payment_2.invoiceStatus = "rejected"
        payment_3.invoiceStatus = "rejected"

        payment_1.setOnClickListener {
            navigate(R.id.paymentDisputeExpandedPage)
        }

        payment_2.setOnClickListener {
            navigate(R.id.paymentDisputeExpandedPage)
        }

        payment_3.setOnClickListener {
            navigate(R.id.paymentDisputeExpandedPage)
        }

        back_button.setOnClickListener { requireActivity().onBackPressed() }

        payment_1.visibility = View.GONE
        payment_2.visibility = View.GONE
        payment_3.visibility = View.GONE
    }
}