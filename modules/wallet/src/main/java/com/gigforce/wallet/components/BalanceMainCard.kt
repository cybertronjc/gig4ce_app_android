package com.gigforce.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.wallet.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.balance_main_card.view.*

class BalanceMainCard : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        View.inflate(context, R.layout.balance_main_card, this)
    }

    var balance: Float = 0F
        set(value) {
            field = value
            val wholePart = value.toInt()
            val fractionPart = value - wholePart
            if (fractionPart == 0F) {
                total_amount.text = String.format("Rs %d", wholePart)
            } else {
                total_amount.text = String.format("Rs %.2f", value)
            }
        }

    var receivedThisMonth: Int = 0
        set(value) {
            field = value
            monthly_received.text = "Rs $value"
        }

    var withdrawnThisMonth: Int = 0
        set(value) {
            field = value
            monthly_withdrawn.text = "Rs $value"
        }
}