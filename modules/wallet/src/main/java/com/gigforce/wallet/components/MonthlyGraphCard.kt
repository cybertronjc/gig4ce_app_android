package com.gigforce.wallet.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.wallet.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.monthly_graph_card.view.*
import kotlinx.android.synthetic.main.recycler_graph_item.view.*
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class MonthlyGraphCard : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    @RequiresApi(Build.VERSION_CODES.O)
    var activeMonth = LocalDateTime.now().monthValue - 1

    var months = ArrayList<String>(
        listOf(
            "-", context.getString(R.string.jan_wallet), context.getString(R.string.feb_wallet), context.getString(
                            R.string.march_wallet), context.getString(R.string.apr_wallet), context.getString(R.string.may_wallet), context.getString(R.string.jun_wallet), context.getString(R.string.jul_wallet),
            context.getString(R.string.aug_wallet), context.getString(R.string.sept_wallet), context.getString(R.string.oct_wallet), context.getString(R.string.nov_wallet), context.getString(R.string.dec_wallet)
        )
    )

    var month: MutableLiveData<Int> = MutableLiveData(6)
    var monthYear: MutableLiveData<String> = MutableLiveData("June 2020")
    var year: Int = 2020

    init {
        View.inflate(context, R.layout.monthly_graph_card, this)
        year = LocalDateTime.now().year
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

        // 12*4 is current year
        graph_recycler.scrollToPosition(12 * 4 + LocalDateTime.now().monthValue - 1)

        prev_month_btn.setOnClickListener {
            Log.d("MGC", "previous month")
            var position =
                (graph_recycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() - 1
            graph_recycler.smoothScrollToPosition(position)
        }

        next_month_btn.setOnClickListener {
            Log.d("MGC", "next month")
            var position =
                (graph_recycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() + 1
            graph_recycler.smoothScrollToPosition(position)
        }

        // use snap helper to update month text
        graph_recycler.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val snapView = snapHelper.findSnapView(graph_recycler.layoutManager)
            val snapPosition = snapView?.let {
                (graph_recycler.layoutManager as GridLayoutManager).getPosition(snapView)
            }
            //Log.d("MCD", snapPosition.toString())
            if (snapPosition != null) {
                //month.value = months[snapPosition%12 + 1]
                month.value = snapPosition % 12 + 1

                setYearFromPosition(snapPosition)

                month_text.text = String.format("%s %04d", months[snapPosition % 12 + 1], year)
            }
        }

    }

    fun setYearFromPosition(position: Int) {
        // 12*4 is current year
        var yearDiff = position - 12 * 4 - LocalDateTime.now().monthValue + 1


        if (yearDiff < 0) {
            // gone backward
            var gap = -1 * yearDiff

            year = LocalDateTime.now().minusMonths(gap.toLong()).year

        } else {
            // gone forward
            year = LocalDateTime.now().plusMonths(yearDiff.toLong()).year
        }

    }
}

class MonthlyGraphAdapter(private val transactions: ArrayList<Int>) :
    RecyclerView.Adapter<MonthlyGraphAdapter.GraphHolder>() {

    var activeMonth = 0

    var monthArray = ArrayList<String>(
        listOf(
            "-", "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
        )
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MonthlyGraphAdapter.GraphHolder {
        val inflatedView =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_graph_item, parent, false)
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

    class GraphHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
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
            } else {
                view.me_status_icon.visibility = View.GONE
                view.me_status_text.visibility = View.GONE
                view.me_amount.text = "0"
                view.arc.progress = 0
            }

        }
    }
}
