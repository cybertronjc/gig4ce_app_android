package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.wallet_balance_card_component.view.*

class WalletBalanceCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.wallet_balance_card_component, this)
    }

    var balance: Int = 0
        set(value) {
            field = value
            amount.text = "Rs $value"
        }
}