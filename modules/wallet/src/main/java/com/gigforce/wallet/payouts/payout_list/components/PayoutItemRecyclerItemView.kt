package com.gigforce.wallet.payouts.payout_list.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.wallet.databinding.RecyclerRowPayoutItemBinding
import com.gigforce.wallet.models.PayoutListPresentationItemData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PayoutItemRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewBinding: RecyclerRowPayoutItemBinding
    private var viewData: PayoutListPresentationItemData.PayoutItemRecyclerItemData? = null

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowPayoutItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val gigerAttendanceData =
                it as PayoutListPresentationItemData.PayoutItemRecyclerItemData
            viewData = gigerAttendanceData


        }
    }

    private fun setStatus(
        status: String,
    ) {

    }

    override fun onClick(v: View?) {
        val currentViewData = viewData ?: return

    }
}
