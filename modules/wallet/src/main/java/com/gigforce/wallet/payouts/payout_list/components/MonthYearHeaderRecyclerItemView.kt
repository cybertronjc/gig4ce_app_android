package com.gigforce.wallet.payouts.payout_list.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.core.IViewHolder
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.RecyclerRowMonthYearHeaderItemViewBinding
import com.gigforce.wallet.models.PayoutListPresentationItemData
import com.gigforce.wallet.payouts.payout_list.PayoutListViewContract

class MonthYearHeaderRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowMonthYearHeaderItemViewBinding
    private var viewData: PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData? = null

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
        viewData = null

        (data as PayoutListPresentationItemData.MonthYearHeaderRecyclerItemData?)?.let {
            viewData = it

            if (it.expanded) {

                viewBinding.statusTv.text = it.date
                viewBinding.statusTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.pink_text, null)
                )
                viewBinding.dropdownView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_up, null)
                )
            } else {

                viewBinding.statusTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey, null)
                )
                viewBinding.dropdownView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_drop, null)
                )
            }
        }
    }

    override fun onClick(
        p0: View?
    ) {
        viewData?.let {
            it.viewModel.handleEvent(PayoutListViewContract.UiEvent.MonthYearHeaderClicked(it))
        }
    }
}
