package com.gigforce.wallet.payouts.payout_list.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.wallet.databinding.RecyclerRowMonthYearHeaderItemViewBinding

class MonthYearHeaderRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder {

    private lateinit var viewBinding: RecyclerRowMonthYearHeaderItemViewBinding

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowMonthYearHeaderItemViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        data?.let {
//            val shiftNameData = it as JoiningList2RecyclerItemData.JoiningListRecyclerStatusItemData
//            viewBinding.statusTv.text = shiftNameData.status
        }
    }
}
