package com.gigforce.wallet

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.core.IEventTracker
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.wallet.models.Invoice
import com.gigforce.wallet.vm.InvoiceViewModel
import com.jay.widget.StickyHeadersLinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_invoices_list.*
import kotlinx.android.synthetic.main.help_expanded_page.*
import javax.inject.Inject

@AndroidEntryPoint
class InvoicesListFragment : WalletBaseFragment() {

    companion object {
        fun newInstance() = InvoicesListFragment()
    }

    @Inject
    lateinit var navigation : INavigation
    @Inject
    lateinit var eventTracker: IEventTracker
    private var win: Window? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invoices_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        changeStatusBarColor()
        listener()
        observer()
    }
    private fun observer() {
       invoiceViewModel.allInvoices.observe(viewLifecycleOwner, Observer {
           run {
               it?.let {
                   Log.d("invoices", it.toString())
                   showInvoices(it)
               }
           }
       })
    }

    private fun showInvoices(invoices: ArrayList<Invoice>) {
        if (invoices.size == 0){
            invoices_error.visible()
            invoices_rv.gone()
        }
        else{
            invoices_error.gone()
            invoices_rv.layoutManager = StickyHeadersLinearLayoutManager<TransactionAdapter>(requireContext())
            invoices_rv.adapter = TransactionAdapter(arrangeTransactions(invoices))

        }
    }

    private fun listener() {
        iv_back_invoices.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun arrangeTransactions(allTransactions: ArrayList<Invoice>): ArrayList<TransactionAdapter.IRow> {
        // sort key is - "YYYYMMDD"
        val transactions = allTransactions.sortedByDescending { it ->
            (String.format("%04d", it.year) + String.format(
                "%02d",
                it.month
            ) + String.format("%02d", it.date)).toInt()
        }

        val result = ArrayList<TransactionAdapter.IRow>()

        var prevMonth = 0
        var prevYear = 0
        for (transaction in transactions) {
            if (prevMonth == transaction.month && prevYear == transaction.year) {
                Log.d("WEP", "Adding SECTION " + transaction.toString())
                // no change in month
                result.add(
                    TransactionAdapter.SectionRow(
                        transaction
                    )
                )
            } else {
                // month change
                Log.d("WEP", "Adding SECTION " + transaction.toString())
                result.add(
                    TransactionAdapter.HeaderRow(
                        String.format("%02d / %d", transaction.month, transaction.year)
                    )
                )

                result.add(
                    TransactionAdapter.SectionRow(
                        transaction
                    )
                )
                prevMonth = transaction.month
                prevYear = transaction.year
            }
        }

        Log.d("WEP", transactions.toString())

        return result
    }

    private fun changeStatusBarColor() {
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_pink))
    }


}

