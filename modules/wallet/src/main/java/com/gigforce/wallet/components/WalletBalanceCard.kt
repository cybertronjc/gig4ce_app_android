package com.gigforce.wallet.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.gigforce.wallet.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.wallet_balance_card_component.view.*
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class WalletBalanceCard : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.wallet_balance_card_component, this)
        last_updated_text.text = String.format(
            "%02d %s %04d",
            LocalDateTime.now().dayOfMonth,
            LocalDateTime.now().month.toString().toLowerCase(),
            LocalDateTime.now().year
        )
    }

    var balance: Float = 0F
        set(value) {
            field = value
            val wholePart = value.toInt()
            val fractionPart = value - wholePart
            if (fractionPart == 0F) {
                amount.text = String.format("Rs %d", wholePart)
            } else {
                amount.text = String.format("Rs %.2f", value)
            }
        }
}