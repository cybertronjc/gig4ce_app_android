package com.gigforce.app.modules.wallet.models

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.wallet.WalletBaseFragment
import com.jay.widget.StickyHeaders
import com.jay.widget.StickyHeadersLinearLayoutManager
import kotlinx.android.synthetic.main.balance_expanded_page.*
import kotlinx.android.synthetic.main.balance_main_card.view.*
import kotlinx.android.synthetic.main.invoice_collapsed_card.view.*
import kotlinx.android.synthetic.main.recview_wallet_month.view.*

class WalletExpandedPage: WalletBaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.balance_expanded_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()

        setListeners()

    }

    private fun initialize() {
        transactions.apply {
            layoutManager = StickyHeadersLinearLayoutManager<TransactionAdapter>(requireContext())
            adapter = TransactionAdapter(ArrayList(arrangeTransactions(invoiceViewModel.generatedInvoice.value!!)))
        }

        walletViewModel.userWallet.observe(viewLifecycleOwner, Observer {
            balance_card.balance = it.balance
        })

    }

    private fun setListeners() {
        help.setOnClickListener { navigate(R.id.helpExpandedPage) }
        back_button.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun arrangeTransactions(allTransactions: ArrayList<Invoice>): ArrayList<TransactionAdapter.IRow> {
        // sort key is - "YYYYMMDD"
        val transactions = allTransactions.sortedByDescending { it ->
            (String.format("%04d", it.year) + String.format("%02d", it.month) + String.format("%02d", it.date)).toInt()
        }

        val result = ArrayList<TransactionAdapter.IRow>()

        var prevMonth = 0
        var prevYear = 0
        for (transaction in transactions) {
            if (prevMonth == transaction.month && prevYear == transaction.year) {
                Log.d("WEP", "Adding SECTION " + transaction.toString())
                // no change in month
                result.add(TransactionAdapter.SectionRow(transaction))
            } else {
                // month change
                result.add(TransactionAdapter.HeaderRow(
                    String.format("%02d / %d", transaction.month, transaction.year)))

                result.add(TransactionAdapter.SectionRow(transaction))
                prevMonth = transaction.month
                prevYear = transaction.year
            }
        }

        Log.d("WEP", transactions.toString())

        return result
    }
}

class TransactionAdapter(private val transactions: ArrayList<IRow>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaders {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SECTION = 1
    }

    interface IRow
    class HeaderRow(val monthString: String): IRow
    class SectionRow(val invoice: Invoice): IRow

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when(viewType) {
            TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.recview_wallet_month, parent, false))

            TYPE_SECTION -> SectionViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.recview_wallet_transaction, parent, false))

            else -> throw IllegalArgumentException()
        }

    override fun getItemCount(): Int {
        return transactions.count()
    }

    override fun getItemViewType(position: Int) = when (transactions[position]) {
            is HeaderRow -> TYPE_HEADER
            is SectionRow -> TYPE_SECTION
            else -> throw IllegalArgumentException()
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_HEADER -> OnBindHeader(holder, transactions[position] as TransactionAdapter.HeaderRow)

            TYPE_SECTION -> OnBindSection(holder, transactions[position] as TransactionAdapter.SectionRow)

            else -> throw IllegalArgumentException()
        }
    }

    private fun OnBindHeader(holder: RecyclerView.ViewHolder, row: HeaderRow) {

        val headerRow = holder as HeaderViewHolder
        headerRow.itemView.month_text.text = row.monthString

    }

    private fun OnBindSection(holder: RecyclerView.ViewHolder, row: SectionRow) {
        (holder as SectionViewHolder).bindTransaction(row.invoice)

    }

    class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {

    }

    class SectionViewHolder(v: View): RecyclerView.ViewHolder(v) {

        private var view: View = v
        private var transaction: Invoice? = null

        init {
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

    override fun isStickyHeader(p0: Int): Boolean {
        Log.d("WEP", (transactions[p0] is HeaderRow).toString())
        return transactions[p0] is HeaderRow
    }
}