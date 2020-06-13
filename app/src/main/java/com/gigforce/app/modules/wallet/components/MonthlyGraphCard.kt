package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.modules.wallet.models.Invoice
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.monthly_graph_card.view.*
import kotlinx.android.synthetic.main.recycler_graph_item.view.*
import java.time.LocalDate

class MonthlyGraphCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        View.inflate(context, R.layout.monthly_graph_card, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun attachAdapter() {
        graph_recycler.apply {
            layoutManager = GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false)
            adapter = MonthlyGraphAdapter(ArrayList((1..500).toList()))
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(graph_recycler)

        graph_recycler.scrollToPosition(12*20 + LocalDate.now().monthValue - 1)

        prev_month_btn.setOnClickListener {
            Log.d("MGC", "previous month")
            graph_recycler.scrollToPosition(
                (graph_recycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() - 1)
        }

        next_month_btn.setOnClickListener {
            Log.d("MGC", "next month")
            graph_recycler.scrollToPosition(
                (graph_recycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() + 1)
        }
    }
}

class MonthlyGraphAdapter(private val transactions: ArrayList<Int>): RecyclerView.Adapter<MonthlyGraphAdapter.GraphHolder>() {

    var activeMonth = 0

    var monthArray = ArrayList<String>(listOf(
        "-", "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    ))
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MonthlyGraphAdapter.GraphHolder {
        val inflatedView = parent.inflate(R.layout.recycler_graph_item, false)
        return GraphHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(
        holder: MonthlyGraphAdapter.GraphHolder,
        position: Int
    ) {
        val itemTransaction = transactions[position]

        holder.bindGraph(monthArray[(position % 12) + 1])
    }

    class GraphHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var transaction: Int = 0

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("RecyclerView", "Click!")
        }

        companion object {
            private val TRANSACTION_KEY = "TRANSACTION"
        }

        fun bindGraph(transaction: String) {
            //view.text.text = transaction
        }
    }
}
