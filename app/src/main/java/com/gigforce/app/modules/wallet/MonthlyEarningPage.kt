package com.gigforce.app.modules.wallet

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.modules.wallet.models.Invoice
import kotlinx.android.synthetic.main.monthly_earning_page.*

class MonthlyEarningPage: WalletBaseFragment() {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MonthlyTransactionAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.monthly_earning_page, inflater, container)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        month_transactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MonthlyTransactionAdapter(invoiceViewModel.monthlyInvoice)
        }
        earning_graph.attachAdapter()
    }
}

class MonthlyTransactionAdapter(private val transactions: ArrayList<Invoice>): RecyclerView.Adapter<MonthlyTransactionAdapter.TransactionHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MonthlyTransactionAdapter.TransactionHolder {
        val inflatedView = parent.inflate(R.layout.invoice_collapsed_card, false)
        return TransactionHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(
        holder: MonthlyTransactionAdapter.TransactionHolder,
        position: Int
    ) {
        val itemTransaction = transactions[position]
        holder.bindTransaction(itemTransaction)
    }

    class TransactionHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
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
        }
    }
}

