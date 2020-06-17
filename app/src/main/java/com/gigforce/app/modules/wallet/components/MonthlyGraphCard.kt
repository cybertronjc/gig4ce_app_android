package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.modules.wallet.models.Invoice
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.monthly_graph_card.view.*
import kotlinx.android.synthetic.main.recycler_graph_item.view.*
import java.time.LocalDate
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class MonthlyGraphCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    @RequiresApi(Build.VERSION_CODES.O)
    var activeMonth = LocalDateTime.now().monthValue - 1

    var months = ArrayList<String>(listOf(
        "-", "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    ))

    var month: MutableLiveData<String> = MutableLiveData("June")

    init {
        View.inflate(context, R.layout.monthly_graph_card, this)
        //month_text.text = months[activeMonth + 1]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun attachAdapter() {
        Log.d("MGC", month_text.text.toString())
        graph_recycler.apply {
            layoutManager = GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false)
            adapter = MonthlyGraphAdapter(ArrayList((1..100).toList()))
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(graph_recycler)

        graph_recycler.scrollToPosition(12*4 + LocalDateTime.now().monthValue - 1)

        prev_month_btn.setOnClickListener {
            Log.d("MGC", "previous month")
            var position = (graph_recycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() - 1
            graph_recycler.smoothScrollToPosition(position)
        }

        next_month_btn.setOnClickListener {
            Log.d("MGC", "next month")
            var position = (graph_recycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() + 1
            graph_recycler.smoothScrollToPosition(position)
        }

        // use snap helper to update month text
        graph_recycler.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val snapView = snapHelper.findSnapView(graph_recycler.layoutManager)
            val snapPosition = snapView?.let { (graph_recycler.layoutManager as GridLayoutManager).getPosition(snapView)}
            //Log.d("MCD", snapPosition.toString())
            if (snapPosition != null) {
                month_text.text = months[snapPosition%12 + 1]
                month.value = months[snapPosition%12 + 1]
            }
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

        fun bindGraph(month: String) {
            //view.text.text = transaction
            //view.me_status_icon.setImageResource()
            if (month == "June") {
                view.me_status_icon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        view.resources,
                        R.drawable.ic_ok,
                        view.context.theme
                    )
                )
                view.me_amount.text = "4000"
                view.arc.progress = 80
            }
            else {
                view.me_status_icon.visibility = View.GONE
                view.me_status_text.visibility = View.GONE
                view.me_amount.text = "0"
                view.arc.progress = 0
            }

        }
    }
}
