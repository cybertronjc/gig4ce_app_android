package com.gigforce.app.modules.wallet.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.wallet.models.Payslip
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.android.synthetic.main.recycler_item_monthly_payslip.view.*

interface MonthlyPayslipsAdapterClickListeners{

    fun downloadPaySlip()
}

class MonthlyPayslipsAdapter constructor(
        private val context: Context
) :
        RecyclerView.Adapter<MonthlyPayslipsAdapter.TimeLineViewHolder>() {

    private var learningVideoActionListener: ((Payslip) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater
    private var mPayslips: List<Payslip> = emptyList()

    fun setOnLearningVideoActionListener(listener: (Payslip) -> Unit) {
        this.learningVideoActionListener = listener
    }

    fun updateCourseContent(payslips: List<Payslip>) {
        this.mPayslips = payslips
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(
                mLayoutInflater.inflate(
                        R.layout.recycler_item_monthly_payslip,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val paySlip = mPayslips[position]
        holder.binTo(paySlip)
    }

    override fun getItemCount() = mPayslips.size

    inner class TimeLineViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val roleTV = itemView.role_tv
        val serialNoTV = itemView.serial_no_text
        val totalPayslipAmountTV = itemView.gig_amount_text
        val monthYearTV = itemView.start_date_text
        val downloadPayslip = itemView.download_payslip_text

        init {
            downloadPayslip.setOnClickListener(this)
        }

        fun binTo(paySlip: Payslip) {
            roleTV.text = paySlip.profile
            serialNoTV.text = "Serial No: ${paySlip.serialNumber}"
            monthYearTV.text = "${paySlip.monthOfPayment} ${paySlip.yearOfPayment}"
            totalPayslipAmountTV.text = "Rs. ${paySlip.totalPayout}"
        }

        override fun onClick(v: View?) {

        }
    }
}
