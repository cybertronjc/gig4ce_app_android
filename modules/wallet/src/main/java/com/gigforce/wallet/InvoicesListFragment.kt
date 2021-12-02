package com.gigforce.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.utils.DocViewerActivity
import com.gigforce.core.IEventTracker
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.wallet.models.InvoiceDataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_invoices_list.*
import javax.inject.Inject

@AndroidEntryPoint
class InvoicesListFragment : WalletBaseFragment() {

    companion object {
        fun newInstance() = InvoicesListFragment()
    }

    @Inject
    lateinit var navigation: INavigation

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
        getIntentData(savedInstanceState)
        initViews()
        changeStatusBarColor()
        listener()
        observer()
    }

    private fun initViews() {
        if (title.isNotBlank())
            tv_invoices_title.text = title
    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }

    private fun observer() {
        invoicesListViewModel.allInvoices.observe(viewLifecycleOwner, Observer {
            run {
                it?.let {
                    Log.d("invoices", it.toString())
                    showInvoices(it)
                }
            }
        })
    }

    private fun showInvoices(invoices: ArrayList<InvoiceDataModel>) {
        if (invoices.size == 0) {
            invoices_error.visible()
            invoices_rv.gone()
        } else {
            invoices_error.gone()
            invoices_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
            invoices_rv.adapter = context?.let { InvoiceListAdapter(it, invoices) }

        }
    }

    private fun listener() {
        iv_back_invoices.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun changeStatusBarColor() {
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        win?.statusBarColor = resources.getColor(R.color.status_bar_pink)
    }


}

class InvoiceListAdapter(
    private val context: Context,
    private val invoices: ArrayList<InvoiceDataModel>
) :
    RecyclerView.Adapter<InvoiceListAdapter.InvoiceViewHolder>() {

//    private var invoiceList: List<JpExplore> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InvoiceViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.invoice_list_item_view, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return invoices.count()
    }


    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        holder.bindValues(invoices.get(position), position)
    }

    inner class InvoiceViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var invoiceNoTv: TextView = itemView.findViewById(R.id.invoice_no)
        private var invoiceDate: TextView = itemView.findViewById(R.id.invoice_date)
        private var invoiceAmount: TextView = itemView.findViewById(R.id.invoice_amount)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(invoiceDataModel: InvoiceDataModel, position: Int) {
            invoiceNoTv.text = invoiceDataModel.invoiceNo
            invoiceDate.text = "Date: " + DateHelper.getDateInDDMMYYYY(invoiceDataModel.invoiceDate)
            invoiceAmount.text = "Rs. " + invoiceDataModel.invoicedAmount.toString()

        }

        override fun onClick(v: View?) {
            val docIntent = Intent(
                context,
                DocViewerActivity::class.java
            )
            docIntent.putExtra(
                StringConstants.DOC_URL.value,
                invoices.get(position).invoiceLink
            )
            docIntent.putExtra(
                StringConstants.DOC_PURPOSE.value,
                "INVOICE"
            )
            context.startActivity(docIntent)
        }

    }

}

