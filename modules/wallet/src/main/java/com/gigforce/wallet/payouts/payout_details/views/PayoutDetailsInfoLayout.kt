package com.gigforce.wallet.payouts.payout_details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.ext.formatToCurrency
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.LayoutPayoutInfoBinding
import com.google.android.material.card.MaterialCardView

class PayoutDetailsInfoLayout(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
) {
    private lateinit var viewBinding: LayoutPayoutInfoBinding

    init {
        setDefault()
        inflate()
        cardElevation = 0.0f
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = LayoutPayoutInfoBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    fun bind(
        data: Payout
    ) = viewBinding.apply {
        this.amountLayout.apply {
            imageView.loadImage(R.drawable.ic_rupee_grey)
            titleTextView.text = "Amount"
            valueTextView.text = ": ${data.amount.formatToCurrency()}"
        }

        this.paidOnLayout.apply {
            imageView.loadImage(R.drawable.ic_calendar_grey)
            titleTextView.text = "Paid On"
            valueTextView.text = ": ${data.getPaidOnDateString()}"
        }

        this.utrNoLayout.apply {
            imageView.loadImage(R.drawable.ic_money_person)
            titleTextView.text = "UTR No"
            valueTextView.text = ": ${data.utrNo ?: "-"}"
        }

        this.categoryLayout.apply {
            imageView.loadImage(R.drawable.ic_menu_grey)
            titleTextView.text = "Category"
            valueTextView.text = ": ${data.category ?: "-"}"
        }

        this.payoutCycleLayout.apply {
            imageView.loadImage(R.drawable.ic_refresh_grey)
            titleTextView.text = "Payout Cycle"
            valueTextView.text = ": ${data.payOutCycle ?: "-"}"
        }
    }
}