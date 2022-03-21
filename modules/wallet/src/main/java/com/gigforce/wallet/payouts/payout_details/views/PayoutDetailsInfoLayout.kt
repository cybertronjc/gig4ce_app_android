package com.gigforce.wallet.payouts.payout_details.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.ext.formatToCurrency
import com.gigforce.common_ui.ext.pushOnclickListener
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.extensions.visible
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
            titleTextView.text = "UTR No."
            valueTextView.text = ": ${data.utrNo ?: "-"}"
            copyText.visible()
            copyText.pushOnclickListener{
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("utr_no",data.utrNo.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context,"${data.utrNo.toString()} copied to clipboard",Toast.LENGTH_LONG).show()
            }
        }

        this.categoryLayout.apply {
            imageView.loadImage(R.drawable.ic_menu_grey)
            titleTextView.text = "Category"
            valueTextView.text = ": ${data.category?.capitalize() ?: "-"}"
        }

        this.payoutCycleLayout.apply {
            imageView.loadImage(R.drawable.ic_refresh_grey)
            titleTextView.text = "Payout Cycle"
            valueTextView.text = ": ${data.payOutCycle ?: "-"}"
        }
    }
}