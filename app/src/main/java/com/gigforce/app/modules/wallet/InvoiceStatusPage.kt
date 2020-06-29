package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.app.R
import com.gigforce.app.modules.roster.px
import com.gigforce.app.modules.wallet.components.InvoiceCollapsedCard
import com.gigforce.app.modules.wallet.models.Invoice
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.all_invoice_status_page.*
import kotlinx.android.synthetic.main.all_invoice_status_page.back_button
import kotlinx.android.synthetic.main.balance_expanded_page.*
import kotlinx.android.synthetic.main.invoice_status_page.*

class InvoiceStatusPage: WalletBaseFragment() {

    lateinit var invoicePageAdapter: InvoicePageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.all_invoice_status_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        invoicePageAdapter = InvoicePageAdapter(this)
        pager.adapter = invoicePageAdapter

        TabLayoutMediator(tabs, pager)  { tab, position ->
            if (position == 0)
                tab.text = "Invoice Generated"
            else if (position == 1)
                tab.text = "Invoice Pending"
            else
                tab.text = "something wrong"
        }.attach()

        back_button.setOnClickListener {
            requireActivity().onBackPressed()
        }

        help_ic.setOnClickListener { navigate(R.id.helpExpandedPage) }
    }

}

class InvoicePageAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = InvoicePageFragment()
        fragment.arguments = Bundle().apply {
            putInt("position", position)
        }
        return fragment
    }

}

class InvoicePageFragment: WalletBaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.invoice_status_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey("position") }?.apply {
            val textView = view.findViewById<MaterialTextView>(R.id.dummy_text)
            val position: Int = getInt("position")
            if (position == 0) {
                // invoice generated

                invoiceViewModel.generatedInvoice.observe(viewLifecycleOwner, Observer {invoices ->
                    addInvoices(invoices_container, invoices)
                })
            } else if (position == 1) {
                // invoice pending

                invoiceViewModel.pendingInvoices.observe(viewLifecycleOwner, Observer { invoices ->
                    //addInvoices(invoices_container, invoices)
                })
            }
        }
    }

   private fun addInvoices(parentView: ConstraintLayout, invoices: ArrayList<Invoice>) {
        // clear constraint layout first in case invoice
        // changes and function is recalled
        parentView.removeAllViews()

        var widgets = ArrayList<InvoiceCollapsedCard>()
        for (invoice in invoices) {
            var widget = InvoiceCollapsedCard(requireContext())
            widget.id = View.generateViewId()

            widget.startDate = invoice.invoiceGeneratedTime
            widget.isInvoiceGenerated = true
            widget.agent = invoice.agentName
            widget.invoiceStatus = invoice.invoiceStatus
            widget.gigAmount = invoice.gigAmount
            widget.gigId = invoice.gigId

            parentView.addView(widget)
            widgets.add(widget)

            widget.layoutParams.width = parentView.width
            widget.requestLayout()
        }

        var constraintSet = ConstraintSet()
        constraintSet.clone(parentView)

        for ((idx, widget) in widgets.withIndex()) {
            constraintSet.connect(widget.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16.px)
            constraintSet.connect(widget.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16.px)

            if (idx == 0) {
                constraintSet.connect(widget.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16.px)
            } else {
                constraintSet.connect(widget.id, ConstraintSet.TOP, widgets[idx-1].id, ConstraintSet.BOTTOM, 16.px)
            }

        }

        constraintSet.applyTo(parentView)
    }
}