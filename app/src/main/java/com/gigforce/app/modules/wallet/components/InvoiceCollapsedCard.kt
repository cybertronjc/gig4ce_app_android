package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.invoice_collapsed_card.view.*

class InvoiceCollapsedCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.invoice_collapsed_card, this)
    }

    var agentIcon: Drawable = resources.getDrawable(R.drawable.wallet)
        set(value) {
            field = value
            icon.setImageDrawable(agentIcon)
        }

    var agent: String = ""
        set(value) {
            field = value
            agent_name.text = agent
        }

    var gigId: Int = 123
        set(value) {
            field = value
            gig_id_text.text = "Gig ID: $gigId"
        }

    var startDate: String = "XX-XX-XXXX"
        set(value) {
            field = value
            start_date_text.text = "Start Date: $startDate"
        }

    var endDate: String = "XX-XX-XXXX"
        set(value) {
            field = value
            end_date_text.text = "End Date: $endDate"
        }

    var gigAmount: Int = 2000
        set(value) {
            field = value
            gig_amount_text.text = "Rs $gigAmount"
        }

    var invoiceStatus: String = "pending"
        set(value) {
            field = value
            gig_invoice_status.text = invoiceStatus

            if (invoiceStatus == "rejected") {
                gig_invoice_status.setTextColor(resources.getColor(R.color.app_red))
            } else {
                gig_invoice_status.setTextColor(resources.getColor(R.color.app_orange))
            }
        }
}