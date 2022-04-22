package com.gigforce.wallet.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.wallet.R
import com.gigforce.wallet.models.Payslip
import kotlinx.android.synthetic.main.recycler_item_monthly_payslip.view.*


class MonthlyPayslipsAdapter constructor(
        private val context: Context
) :
        RecyclerView.Adapter<MonthlyPayslipsAdapter.TimeLineViewHolder>() {

    private var paySlipClickActionListener: ((Payslip) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater
    private var mPayslips: List<Payslip> = emptyList()

    fun setOnPaySlipClickActionListener(listener: (Payslip) -> Unit) {
        this.paySlipClickActionListener = listener
    }

    fun updateCourseContent(payslips: List<Payslip>) {
        this.mPayslips = payslips
        notifyDataSetChanged()
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

        init {
            itemView.setOnClickListener(this)
        }

        fun binTo(paySlip: Payslip) {
            roleTV.text = paySlip.profile.capitalize()
            serialNoTV.text = context.getString(R.string.serial_no_wallet) + paySlip.serialNumber
            monthYearTV.text = "${paySlip.monthOfPayment} ${paySlip.yearOfPayment}"
            totalPayslipAmountTV.text = "Rs. ${paySlip.totalPayout}"
        }

        override fun onClick(v: View?) {
            val paySlip = mPayslips[adapterPosition]
            paySlipClickActionListener?.invoke(paySlip)
        }
    }
}
