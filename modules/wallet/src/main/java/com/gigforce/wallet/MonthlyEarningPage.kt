package com.gigforce.wallet

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.wallet.adapters.InvoiceAdapter
import com.gigforce.wallet.models.Invoice
import com.gigforce.core.navigation.INavigation
import com.jay.widget.StickyHeadersLinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.invoice_collapsed_card.view.*
import kotlinx.android.synthetic.main.monthly_earning_page.*
import javax.inject.Inject

@AndroidEntryPoint
class MonthlyEarningPage : WalletBaseFragment() {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MonthlyTransactionAdapter
    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.monthly_earning_page, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        earning_graph.attachAdapter()

        earning_graph.month.observe(viewLifecycleOwner, Observer {
            val mnth = it
            val year = earning_graph.year

            monthly_text.text = String.format("%02d / %04d", mnth, year)

            month_transactions.apply {
                layoutManager = StickyHeadersLinearLayoutManager<InvoiceAdapter>(requireContext())
                adapter = InvoiceAdapter(
                    ArrayList(
                        InvoiceAdapter.arrangeTransactions(
                            invoiceViewModel.getMonthlyInvoices(
                                invoiceViewModel.allInvoices.value, mnth, year
                            )
                        )
                    )
                )
            }

        })

    }

    private fun setListeners() {
        back_button.setOnClickListener { requireActivity().onBackPressed() }
        help_ic.setOnClickListener { navigation.navigateTo("wallet/helpExpandedPage") }

    }
}

class MonthlyTransactionAdapter(private val transactions: ArrayList<Invoice>) :
    RecyclerView.Adapter<MonthlyTransactionAdapter.TransactionHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.invoice_collapsed_card,parent,false)
        return TransactionHolder(
            inflatedView
        )
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(
        holder: TransactionHolder,
        position: Int
    ) {
        val itemTransaction = transactions[position]
        holder.bindTransaction(itemTransaction)
    }

    class TransactionHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var transaction: Invoice? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("RecyclerView", "Click!")
        }

        companion object {
            private val TRANSACTION_KEY = "TRANSACTION"
        }

        fun bindTransaction(transaction: Invoice) {
            this.transaction = transaction
            view.start_date_text.text = "Invoice Generated ${transaction.invoiceGeneratedTime}"
            view.end_date_text.text = ""
            view.gig_invoice_status.text = "processed"
            view.gig_invoice_status.setTextColor(view.resources.getColor(R.color.app_green))
            view.agent_name.text = transaction.agentName
            view.gig_amount_text.text = "Rs ${transaction.gigAmount}"
            view.gig_id_text.text = "Gig Id: ${transaction.gigId}"
        }
    }
}

