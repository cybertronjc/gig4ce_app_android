package com.gigforce.wallet.payouts.payout_list.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.ext.formatToCurrency
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.capitalizeWords
import com.gigforce.core.navigation.INavigation
import com.gigforce.wallet.PayoutNavigation
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.RecyclerRowPayoutItemBinding
import com.gigforce.wallet.models.PayoutListPresentationItemData
import com.gigforce.wallet.payouts.payout_list.PayoutListViewContract
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import java.util.*
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

    private lateinit var viewBinding: RecyclerRowPayoutItemBinding
    private var viewData: PayoutListPresentationItemData.PayoutItemRecyclerItemData? = null

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val paidOnDateFormatter = DateTimeFormatter.ofPattern("dd/LLL/yyyy") //YYYY-MM-DD

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

        (data as PayoutListPresentationItemData.PayoutItemRecyclerItemData?)?.let {
            viewData = it

            viewBinding.amountTextview.text = it.amount.formatToCurrency()
            viewBinding.businessTextview.text = it.companyName?.capitalize(Locale.getDefault())
            viewBinding.categoryTextview.text = it.category
            viewBinding.payoutStatusView.bind(
                it.status,
                it.statusColorCode
            )
            viewBinding.paidOnTextview.text = formatPaymentDate(it.paymentDate)

            if (it.icon != null) {
                viewBinding.businessLogoIv.loadImageIfUrlElseTryFirebaseStorage(
                    it.icon
                )
            } else {
                val businessInitials: String = if (it.companyName != null) {
                    it.companyName[0].uppercaseChar().toString()
                } else {
                    "C"
                }
                val drawable = TextDrawable.builder().buildRound(
                    businessInitials,
                    ResourcesCompat.getColor(resources, R.color.lipstick, null)
                )
                viewBinding.businessLogoIv.setImageDrawable(drawable)
            }
        }
    }

    private fun formatPaymentDate(
        paymentDate: String?
    ): String {
        return if (paymentDate != null) {
            "Paid on : ${paidOnDateFormatter.format(isoDateFormatter.parse(paymentDate))}"
        } else {
            "Paid on : -"
        }
    }

    override fun onClick(v: View?) {

        val currentViewData = viewData ?: return
        currentViewData.viewModel.handleEvent(
            PayoutListViewContract.UiEvent.PayoutItemClicked(
                currentViewData
            )
        )
    }
}
