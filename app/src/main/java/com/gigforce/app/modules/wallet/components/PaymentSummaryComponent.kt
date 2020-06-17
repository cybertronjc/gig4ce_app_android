package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.app.R
import kotlinx.android.synthetic.main.payment_summary_component.view.*

class PaymentSummaryComponent: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.payment_summary_component, this)
    }

    var monthlyEarning: Int = 0
        set(value) {
            field = value
            monthly_earning_amount.text = "Rs $value"
        }

    var invoiceAmount: Int = 0
        set(value) {
            field = value
            invoice_status_amount.text = "Rs $value"
        }

    var paymentDueAmount: Int = 0
        set(value) {
            field = value
            payment_dispute_amount.text = "Rs $value"
        }

}