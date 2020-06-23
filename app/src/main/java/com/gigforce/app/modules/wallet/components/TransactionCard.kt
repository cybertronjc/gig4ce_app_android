package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.transaction_card.view.*

class TransactionCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs:AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.transaction_card, this)
    }

    var timings: String = ""
        set(value) {
            field = value
            t_timings.text = value
        }

    var amount: Int = 0
        set(value) {
            field = value
            t_amount.text = "Rs $value"
        }

    var agent: String = ""
        set(value) {
            field = value
            t_title.text = value
        }

    var status: String = ""
        set(value) {
            field = value
            t_status.text = value
        }
}