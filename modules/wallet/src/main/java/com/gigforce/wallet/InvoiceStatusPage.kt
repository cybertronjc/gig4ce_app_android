package com.gigforce.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.core.extensions.px
import com.gigforce.core.navigation.INavigation
import com.gigforce.wallet.components.InvoiceCollapsedCard
import com.gigforce.wallet.models.Invoice
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.all_invoice_status_page.*
import kotlinx.android.synthetic.main.invoice_status_page.*
import javax.inject.Inject

@AndroidEntryPoint
class InvoiceStatusPage : WalletBaseFragment() {

    lateinit var invoicePageAdapter: InvoicePageAdapter
    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.all_invoice_status_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        invoicePageAdapter = InvoicePageAdapter(this)
        pager.adapter = invoicePageAdapter

        TabLayoutMediator(tabs, pager) { tab, position ->
            if (position == 0)
                tab.text = getString(R.string.generated_wallet)
            else if (position == 1)
                tab.text = getString(R.string.invoice_pending_wallet)
            else
                tab.text = getString(R.string.something_wrong_wallet)
        }.attach()

        back_button.setOnClickListener {
            requireActivity().onBackPressed()
        }

        help_ic.setOnClickListener { navigation.navigateTo("wallet/helpExpandedPage") }
    }

}

class InvoicePageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
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

class InvoicePageFragment : WalletBaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.invoice_status_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey("position") }?.apply {
            val textView = view.findViewById<MaterialTextView>(R.id.dummy_text)
            val position: Int = getInt("position")
            if (position == 0) {
                // invoice generated

                invoiceViewModel.generatedInvoice.observe(viewLifecycleOwner, Observer { invoices ->
                    addInvoices(invoices_container, invoiceViewModel.getGeneratedInvoices(invoices))
                })
            } else if (position == 1) {
                // invoice pending

                invoiceViewModel.pendingInvoices.observe(viewLifecycleOwner, Observer { invoices ->
                    addInvoices(invoices_container, invoiceViewModel.getPendingInvoices(invoices))
                })
            }
        }
    }

    private fun addInvoices(parentView: ConstraintLayout, invoices: ArrayList<Invoice>) {
        if (invoices.size == 0)
            return
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
            constraintSet.connect(
                widget.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                16.px
            )
            constraintSet.connect(
                widget.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                16.px
            )

            if (idx == 0) {
                constraintSet.connect(
                    widget.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    16.px
                )
            } else {
                constraintSet.connect(
                    widget.id,
                    ConstraintSet.TOP,
                    widgets[idx - 1].id,
                    ConstraintSet.BOTTOM,
                    16.px
                )
            }

        }

        constraintSet.applyTo(parentView)
    }
}