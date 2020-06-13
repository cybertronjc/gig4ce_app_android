package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.payment_disputed_expanded_card.view.*

class PaymentDisputedExpandedCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        View.inflate(context, R.layout.payment_disputed_expanded_card, this)
    }

    var disputedHeading: String = ""
        set(value) {
            field = value
            disputed_heading.text = value
        }

    var gigAmount: Int = 0
        set(value) {
            field = value
            gig_amount.text = "Rs $gigAmount"
        }

    var dateTimeText: String = ""
        set(value) {
            field = value
            date_time_text.text = dateTimeText
        }
}