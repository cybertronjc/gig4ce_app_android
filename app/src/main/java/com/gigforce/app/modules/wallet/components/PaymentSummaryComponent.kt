package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.app.R
import kotlinx.android.synthetic.main.payment_summary_component.view.*
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class PaymentSummaryComponent: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.payment_summary_component, this)

        monthly_earning_card_text.text = LocalDateTime.now().month.toString().toLowerCase()

        payment_dispute_card_text.text = String.format(
            "Last updated, %02d %s %04d", LocalDateTime.now().dayOfMonth, LocalDateTime.now().month.toString().toLowerCase(), LocalDateTime.now().year )
    }

    var monthlyEarning: Int = 0
        set(value) {
            field = value
            monthly_earning_amount.text = "Rs $value"
        }

    var invoiceAmount: Int = 0
        set(value) {
            field = value
        }

    var paymentDueAmount: Int = 0
        set(value) {
            field = value
            payment_dispute_amount.text = "Rs $value"
        }

}